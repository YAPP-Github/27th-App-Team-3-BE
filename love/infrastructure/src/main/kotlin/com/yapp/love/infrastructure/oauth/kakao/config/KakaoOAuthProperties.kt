package com.yapp.love.infrastructure.oauth.kakao.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kakao.oauth")
data class KakaoOAuthProperties(
    val clientId: String
)
