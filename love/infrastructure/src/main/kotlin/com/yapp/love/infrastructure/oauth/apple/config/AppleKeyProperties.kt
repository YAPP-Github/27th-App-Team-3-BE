package com.yapp.love.infrastructure.oauth.apple.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "apple.key")
data class AppleKeyProperties(
    val path: String,
)
