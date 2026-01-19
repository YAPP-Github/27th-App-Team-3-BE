package com.yapp.love.infrastructure.oauth.kakao.response

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class KakaoTokenResponse(
    val accessToken: String,
    val idToken: String,
    val refreshToken: String?,
    val expiresIn: Int,
    val tokenType: String,
    val scope: String?,
)
