package com.yapp.love.infrastructure.oauth.google.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "google.oauth")
data class GoogleOAuthProperties(
    val clientId: String,
    val iosClientId: String,
    val androidClientIds: List<String>,
    val clientSecret: String,
)
