package com.yapp.love.web.notification.dto.response

import com.yapp.love.domain.notification.model.NotificationType
import java.time.LocalDateTime

data class NotificationResponse(
    val id: Long,
    val type: NotificationType,
    val title: String,
    val body: String,
    val deepLink: String?,
    val isRead: Boolean,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(notification: com.yapp.love.domain.notification.model.Notification) =
            NotificationResponse(
                id = notification.id!!,
                type = notification.type,
                title = notification.title,
                body = notification.body,
                deepLink = notification.deepLink,
                isRead = notification.isRead,
                createdAt = notification.createdAt,
            )
    }
}
