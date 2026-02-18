package com.yapp.love.application.notification

import com.yapp.love.application.notification.port.FcmPushService
import com.yapp.love.domain.notification.NotificationRepository
import com.yapp.love.domain.notification.model.Notification
import com.yapp.love.domain.notification.model.NotificationType
import com.yapp.love.globalutils.exception.GlobalErrorCode
import com.yapp.love.globalutils.exception.GlobalException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val notificationSettingService: NotificationSettingService,
    private val fcmPushService: FcmPushService,
) {
    @Transactional(readOnly = true)
    fun getNotifications(userId: Long): List<Notification> {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
    }

    @Transactional
    fun markAsRead(userId: Long, notificationId: Long) {
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

        // 1. 알림 센터에 저장 (notificationId 확보)
        val notification =
            Notification.create(
                userId = targetUserId,
                type = type,
                title = title,
                body = body,
            )
        val saved = notificationRepository.save(notification)

        // 2. notificationId를 포함한 딥링크 생성 후 업데이트
        val allParams = buildMap {
            put("notificationId", saved.id.toString())
            putAll(deepLinkParams)
        }
        val deepLink = type.makeDeepLink(allParams)
        saved.deepLink = deepLink
        notificationRepository.save(saved)

        // 3. FCM 푸시 전송
        if (notificationSettingService.shouldSendPush(targetUserId, type)) {
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
            logger.info { "푸시 설정에 의해 생략: userId=$targetUserId, type=$type" }
        }
    }
}