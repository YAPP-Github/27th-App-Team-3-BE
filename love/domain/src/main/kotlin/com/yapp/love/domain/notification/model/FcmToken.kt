package com.yapp.love.domain.notification.model

import com.yapp.love.domain.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "fcm_tokens",
    indexes = [
        Index(name = "idx_fcm_tokens_user", columnList = "user_id"),
    ],
)
class FcmToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Column(nullable = false, length = 500)
    var token: String,
    @Column(name = "device_id", nullable = false, length = 100)
    val deviceId: String,
) : BaseEntity() {
    fun updateToken(newToken: String) {
        this.token = newToken
    }

    companion object {
        fun create(
            userId: Long,
            token: String,
            deviceId: String,
        ) = FcmToken(
            userId = userId,
            token = token,
            deviceId = deviceId,
        )
    }
}
