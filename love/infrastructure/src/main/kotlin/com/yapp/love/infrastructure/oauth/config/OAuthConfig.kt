package com.yapp.love.infrastructure.oauth.config

import com.yapp.love.infrastructure.oauth.apple.config.AppleKeyProperties
import com.yapp.love.infrastructure.oauth.apple.config.AppleOauthProperties
import com.yapp.love.infrastructure.oauth.google.config.GoogleOAuthProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    AppleOauthProperties::class,
    AppleKeyProperties::class,
    GoogleOAuthProperties::class,
)
class OAuthConfig
