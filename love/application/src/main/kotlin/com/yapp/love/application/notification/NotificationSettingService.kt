package com.yapp.love.application.notification

import com.yapp.love.application.notification.port.FcmPushService
import com.yapp.love.domain.notification.FcmTokenRepository
import com.yapp.love.domain.notification.NotificationSettingRepository
import com.yapp.love.domain.notification.model.NotificationSetting
import com.yapp.love.domain.notification.model.NotificationType
import com.yapp.love.globalutils.exception.GlobalErrorCode
import com.yapp.love.globalutils.exception.GlobalException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalTime

@Service
class NotificationSettingService(
    private val notificationSettingRepository: NotificationSettingRepository,
    private val fcmTokenRepository: FcmTokenRepository,
    private val fcmPushService: FcmPushService,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    companion object {
        private const val NIGHT_START_HOUR = 21
        private const val NIGHT_END_HOUR = 8
    }

    @Transactional
    fun initSetting(
        userId: Long,
        isPokePushEnabled: Boolean,
        isMarketingPushEnabled: Boolean,
        isNightPushEnabled: Boolean,
    ): NotificationSetting {
        val setting = notificationSettingRepository.findByUserId(userId)
            ?: NotificationSetting.create(userId = userId)
        setting.updatePokePush(isPokePushEnabled)
        setting.updateMarketingPush(isMarketingPushEnabled)
        setting.updateNightPush(isNightPushEnabled)
        return notificationSettingRepository.save(setting)
    }

    @Transactional
    fun getSetting(userId: Long): NotificationSetting {
        return findByUserId(userId)
    }

    @Transactional
    fun updatePokePush(userId: Long, enabled: Boolean): NotificationSetting {
        val setting = findByUserId(userId)
        setting.updatePokePush(enabled)
        return notificationSettingRepository.save(setting)
    }

    @Transactional
    fun updateMarketingPush(userId: Long, enabled: Boolean): NotificationSetting {
        val setting = findByUserId(userId)
        setting.updateMarketingPush(enabled)
        notificationSettingRepository.save(setting)

        val tokens = fcmTokenRepository.findByUserId(userId).map { it.token }
        tokens.forEach { token ->
            if (enabled) {
                fcmPushService.subscribeToTopic(token, FcmTokenService.MARKETING_TOPIC)
            } else {
                fcmPushService.unsubscribeFromTopic(token, FcmTokenService.MARKETING_TOPIC)
            }
        }

        return setting
    }

    @Transactional
    fun updateNightPush(userId: Long, enabled: Boolean): NotificationSetting {
        val setting = findByUserId(userId)
        setting.updateNightPush(enabled)
        return notificationSettingRepository.save(setting)
    }

    fun shouldSendPush(userId: Long, type: NotificationType): Boolean {
        val setting = notificationSettingRepository.findByUserId(userId) ?: return false

        when (type) {
            NotificationType.POKE -> if (!setting.isPokePushEnabled) return false
            else -> {}
        }

        if (isNightTime() && !setting.isNightPushEnabled) return false

        return true
    }

    private fun findByUserId(userId: Long): NotificationSetting {
        return notificationSettingRepository.findByUserId(userId)
            ?: notificationSettingRepository.save(NotificationSetting.create(
                userId = userId,
                isPokePushEnabled = false,
                isMarketingPushEnabled = false,
                isNightPushEnabled = false,
            ))
    }

    private fun isNightTime(): Boolean {
        val hour = LocalTime.now(clock).hour
        return hour >= NIGHT_START_HOUR || hour < NIGHT_END_HOUR
    }
}
