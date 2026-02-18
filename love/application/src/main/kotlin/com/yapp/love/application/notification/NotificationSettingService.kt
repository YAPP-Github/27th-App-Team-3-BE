package com.yapp.love.application.notification

import com.yapp.love.domain.notification.NotificationSettingRepository
import com.yapp.love.domain.notification.model.NotificationSetting
import com.yapp.love.domain.notification.model.NotificationType
import com.yapp.love.globalutils.exception.GlobalErrorCode
import com.yapp.love.globalutils.exception.GlobalException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalTime

@Service
class NotificationSettingService(
    private val notificationSettingRepository: NotificationSettingRepository,
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
        val setting = NotificationSetting.create(
            userId = userId,
            isPokePushEnabled = isPokePushEnabled,
            isMarketingPushEnabled = isMarketingPushEnabled,
            isNightPushEnabled = isNightPushEnabled,
        )
        return notificationSettingRepository.save(setting)
    }

    @Transactional(readOnly = true)
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
            NotificationType.MARKETING -> if (!setting.isMarketingPushEnabled) return false
            else -> {}
        }

        if (isNightTime() && !setting.isNightPushEnabled) return false

        return true
    }

    private fun findByUserId(userId: Long): NotificationSetting {
        return notificationSettingRepository.findByUserId(userId)
            ?: throw GlobalException(GlobalErrorCode.NOT_FOUND, "알림 설정을 찾을 수 없습니다.")
    }

    private fun isNightTime(): Boolean {
        val hour = LocalTime.now().hour
        return hour >= NIGHT_START_HOUR || hour < NIGHT_END_HOUR
    }
}