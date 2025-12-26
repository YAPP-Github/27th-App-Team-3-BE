package com.yapp.love.infrastructure.oauth.apple.dto

data class AppleTokenResult(
    val accessToken: String,
    val idToken: String,
    val refreshToken: String?,
)
