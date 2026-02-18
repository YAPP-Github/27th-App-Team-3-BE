package com.yapp.love.domain.notification.model

import com.yapp.love.domain.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "notification_settings")
class NotificationSetting(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "user_id", nullable = false, unique = true)
    val userId: Long,
    @Column(name = "is_notification_enabled", nullable = false)
    var isPushNotificationEnabled: Boolean = false,
    @Column(name = "is_night_notification_enabled", nullable = false)
    var isNightPushNotificationEnabled: Boolean = false,
) : BaseEntity() {
    fun updatePushNotification(enabled: Boolean) {
        isPushNotificationEnabled = enabled
    }
    fun updateNightPushNotification(enabled: Boolean) {
        isNightPushNotificationEnabled = enabled
    }

    companion object {
        fun create(userId: Long) = NotificationSetting(userId = userId)
    }
}
