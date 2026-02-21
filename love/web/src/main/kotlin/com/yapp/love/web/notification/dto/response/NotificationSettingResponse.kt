package com.yapp.love.web.notification.dto.response

import com.yapp.love.domain.notification.model.NotificationSetting

data class NotificationSettingResponse(
    val isPushEnabled: Boolean,
    val isMarketingPushEnabled: Boolean,
    val isNightPushEnabled: Boolean,
) {
    companion object {
        fun from(setting: NotificationSetting) = NotificationSettingResponse(
            isPushEnabled = setting.isPokePushEnabled,
            isMarketingPushEnabled = setting.isMarketingPushEnabled,
            isNightPushEnabled = setting.isNightPushEnabled,
        )
    }
}
