package com.yapp.love.infrastructure.oauth.apple.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "apple.oauth")
data class AppleOauthProperties(
    val clientId: String,
    val teamId: String,
    val keyId: String,
    val aud: String,
)
