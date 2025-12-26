package com.yapp.love.domain.user.model

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
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["oauth_provider", "oauth_provider_id"]),
    ],
)
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val email: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false)
    val oauthProvider: SocialProvider,
    @Column(name = "oauth_provider_id", nullable = false)
    val oauthProviderId: String,
)
