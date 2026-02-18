package com.yapp.love.domain.notification

import com.yapp.love.domain.notification.model.Notification

interface NotificationRepository {
    fun save(notification: Notification): Notification

    fun findById(id: Long): Notification?

    fun findByUserId(userId: Long): List<Notification>

    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<Notification>

    fun countUnreadByUserId(userId: Long): Long
}
