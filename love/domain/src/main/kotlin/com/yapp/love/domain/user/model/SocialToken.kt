package com.yapp.love.domain.user.model

import com.yapp.love.domain.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "social_tokens",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "provider"]),
    ],
)
class SocialToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val provider: SocialProvider,
    @Column(name = "refresh_token", nullable = false, length = 1024)
    var refreshToken: String,
) : BaseEntity() {
    fun updateRefreshToken(refreshToken: String) {
        this.refreshToken = refreshToken
    }

    companion object {
        fun create(
            userId: Long,
            provider: SocialProvider,
            refreshToken: String,
        ) = SocialToken(
            userId = userId,
            provider = provider,
            refreshToken = refreshToken,
        )
    }
}