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
    @Column(name = "is_poke_push_enabled", nullable = false)
    var isPokePushEnabled: Boolean = false,
    @Column(name = "is_marketing_push_enabled", nullable = false)
    var isMarketingPushEnabled: Boolean = false,
    @Column(name = "is_night_push_enabled", nullable = false)
    var isNightPushEnabled: Boolean = false,
) : BaseEntity() {
    fun updatePokePush(enabled: Boolean) {
        isPokePushEnabled = enabled
    }
    fun updateMarketingPush(enabled: Boolean) {
        isMarketingPushEnabled = enabled
    }
    fun updateNightPush(enabled: Boolean) {
        isNightPushEnabled = enabled
    }

    companion object {
        fun create(
            userId: Long,
            isPokePushEnabled: Boolean = false,
            isMarketingPushEnabled: Boolean = false,
            isNightPushEnabled: Boolean = false,
        ) = NotificationSetting(
            userId = userId,
            isPokePushEnabled = isPokePushEnabled,
            isMarketingPushEnabled = isMarketingPushEnabled,
            isNightPushEnabled = isNightPushEnabled,
        )
    }
}
