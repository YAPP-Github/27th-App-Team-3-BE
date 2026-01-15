package com.yapp.love.application.auth.dto

data class TokenRefreshResult(
    val accessToken: String,
    val refreshToken: String,
)
