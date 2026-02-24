package com.yapp.love.domain.notification

import com.yapp.love.domain.notification.model.NotificationSetting

interface NotificationSettingRepository {
    fun save(notificationSetting: NotificationSetting): NotificationSetting

    fun findByUserId(userId: Long): NotificationSetting?

    fun findAllByIsMarketingPushEnabledTrue(): List<NotificationSetting>
}
