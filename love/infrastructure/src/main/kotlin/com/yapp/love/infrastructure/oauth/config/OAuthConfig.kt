package com.yapp.love.infrastructure.oauth.config

import com.yapp.love.infrastructure.oauth.apple.config.AppleKeyProperties
import com.yapp.love.infrastructure.oauth.apple.config.AppleOAuthProperties
import com.yapp.love.infrastructure.oauth.google.config.GoogleOAuthProperties
import com.yapp.love.infrastructure.oauth.kakao.config.KakaoOAuthProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    AppleOAuthProperties::class,
    AppleKeyProperties::class,
    GoogleOAuthProperties::class,
    KakaoOAuthProperties::class,
)
class OAuthConfig
