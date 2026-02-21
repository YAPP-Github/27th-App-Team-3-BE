package com.yapp.love.domain.notification

import com.yapp.love.domain.notification.model.Notification
import java.time.LocalDateTime

interface NotificationRepository {
    fun save(notification: Notification): Notification

    fun findById(id: Long): Notification?

    fun findByUserIdWithCursor(userId: Long, lastId: Long?, size: Int): List<Notification>

    fun existsUnreadByUserId(userId: Long): Boolean

    fun markAllAsReadByUserId(userId: Long)

    fun deleteCreatedBefore(threshold: LocalDateTime): Int
}
