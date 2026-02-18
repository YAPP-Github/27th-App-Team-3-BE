package com.yapp.love.application.notification

import com.yapp.love.application.notification.port.FcmPushService
import com.yapp.love.domain.notification.FcmTokenRepository
import com.yapp.love.domain.notification.NotificationRepository
import com.yapp.love.domain.notification.NotificationSettingRepository
import com.yapp.love.domain.notification.model.FcmToken
import com.yapp.love.domain.notification.model.Notification
import com.yapp.love.domain.notification.model.NotificationSetting
import com.yapp.love.domain.notification.model.NotificationType
import com.yapp.love.globalutils.exception.GlobalErrorCode
import com.yapp.love.globalutils.exception.GlobalException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalTime

private val logger = KotlinLogging.logger {}

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val fcmTokenRepository: FcmTokenRepository,
    private val notificationSettingRepository: NotificationSettingRepository,
    private val fcmPushService: FcmPushService,
) {
    @Transactional
    fun registerFcmToken(
        userId: Long,
        token: String,
        deviceId: String,
    ) {
        val existingToken = fcmTokenRepository.findByUserIdAndDeviceId(userId, deviceId)

        if (existingToken != null) {
            existingToken.updateToken(token)
            fcmTokenRepository.save(existingToken)
        } else {
            fcmTokenRepository.save(FcmToken.create(userId, token, deviceId))
        }
    }

    @Transactional(readOnly = true)
    fun getNotifications(userId: Long): List<Notification> {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
    }

    @Transactional
    fun markAsRead(
        userId: Long,
        notificationId: Long,
    ) {
        val notification =
            notificationRepository.findById(notificationId)
                ?: throw GlobalException(GlobalErrorCode.NOT_FOUND, "알림을 찾을 수 없습니다.")

        require(notification.userId == userId) { "본인의 알림만 읽음 처리할 수 있습니다." }

        notification.markAsRead()
        notificationRepository.save(notification)
    }

    @Transactional
    fun markAllAsRead(userId: Long) {
        val notifications = notificationRepository.findByUserId(userId)
        notifications.filter { !it.isRead }.forEach { notification ->
            notification.markAsRead()
            notificationRepository.save(notification)
        }
    }

    @Transactional(readOnly = true)
    fun getNotificationSetting(userId: Long): NotificationSetting {
        return notificationSettingRepository.findByUserId(userId)
            ?: NotificationSetting.create(userId)
    }

    @Transactional
    fun updatePushNotification(
        userId: Long,
        enabled: Boolean,
    ): NotificationSetting {
        val setting =
            notificationSettingRepository.findByUserId(userId)
                ?: NotificationSetting.create(userId)
        setting.updatePushNotification(enabled)
        return notificationSettingRepository.save(setting)
    }

    @Transactional
    fun updateNightPushNotification(
        userId: Long,
        enabled: Boolean,
    ): NotificationSetting {
        val setting =
            notificationSettingRepository.findByUserId(userId)
                ?: NotificationSetting.create(userId)
        setting.updateNightPushNotification(enabled)
        return notificationSettingRepository.save(setting)
    }

    @Transactional
    fun sendNotification(
        targetUserId: Long,
        type: NotificationType,
        titleArgs: Array<Any> = emptyArray(),
        bodyArgs: Array<Any> = emptyArray(),
        deepLinkParams: Map<String, String> = emptyMap(),
    ) {
        val title = type.formatTitle(*titleArgs)
        val body = type.formatBody(*bodyArgs)
        val deepLink = type.makeDeepLink(deepLinkParams)

        // 타입1: 알림 센터에 저장
        val notification =
            Notification.create(
                userId = targetUserId,
                type = type,
                title = title,
                body = body,
                deepLink = deepLink,
            )
        notificationRepository.save(notification)

        // 타입2: FCM 푸시 전송 (야간 알림 미동의 시 야간에는 푸시 생략)
        if (shouldSendPush(targetUserId)) {
            val sent =
                fcmPushService.sendPushToUser(
                    userId = targetUserId,
                    title = title,
                    body = body,
                    deepLink = deepLink,
                )

            if (!sent) {
                logger.warn { "FCM 푸시 전송 실패: userId=$targetUserId, type=$type" }
            }
        } else {
            logger.info { "야간 시간대 푸시 생략: userId=$targetUserId, type=$type" }
        }
    }

    private fun shouldSendPush(userId: Long): Boolean {
        if (!isNightTime()) return true
        val setting = notificationSettingRepository.findByUserId(userId) ?: return false
        return setting.isNightPushNotificationEnabled
    }

    private fun isNightTime(): Boolean {
        val hour = LocalTime.now().hour
        return hour >= NIGHT_START_HOUR || hour < NIGHT_END_HOUR
    }

    companion object {
        private const val NIGHT_START_HOUR = 21
        private const val NIGHT_END_HOUR = 8
    }
}
