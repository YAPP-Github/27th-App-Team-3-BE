package com.yapp.love.infrastructure.oauth.kakao.response

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class KakaoTokenInfoResponse(
    val iss: String,
    val aud: String,
    val sub: String,
    val iat: Long,
    val exp: Long,
    val authTime: Long,
    val nonce: String?,
    val nickname: String?,
    val picture: String?,
    val email: String?,
)
