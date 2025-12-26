package com.yapp.love.application.auth.dto

data class OAuthLoginResult(
    val userId: Long,
    val accessToken: String,
    val refreshToken: String,
    val isNewUser: Boolean,
)
