package com.yapp.love.application.notification

import com.yapp.love.domain.notification.NotificationSettingRepository
import com.yapp.love.domain.notification.model.NotificationSetting
import com.yapp.love.domain.notification.model.NotificationType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationSettingService(
    private val notificationSettingRepository: NotificationSettingRepository,
) {
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
        return notificationSettingRepository.save(setting)
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

        return true
    }

    private fun findByUserId(userId: Long): NotificationSetting {
        return notificationSettingRepository.findByUserId(userId)
            ?: notificationSettingRepository.save(
                NotificationSetting.create(
                    userId = userId,
                    isPokePushEnabled = false,
                    isMarketingPushEnabled = false,
                    isNightPushEnabled = false,
                ),
            )
    }
}