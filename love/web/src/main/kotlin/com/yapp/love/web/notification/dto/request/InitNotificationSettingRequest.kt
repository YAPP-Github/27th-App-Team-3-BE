package com.yapp.love.web.notification.dto.request

data class InitNotificationSettingRequest(
    val isPushEnabled: Boolean,
    val isMarketingPushEnabled: Boolean,
    val isNightPushEnabled: Boolean,
)
