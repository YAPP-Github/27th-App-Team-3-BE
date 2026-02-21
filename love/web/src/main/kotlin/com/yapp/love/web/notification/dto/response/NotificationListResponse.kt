package com.yapp.love.web.notification.dto.response

data class NotificationListResponse(
    val notifications: List<NotificationResponse>,
    val hasNext: Boolean,
)
