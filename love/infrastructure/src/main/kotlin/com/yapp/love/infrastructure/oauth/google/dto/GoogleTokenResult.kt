package com.yapp.love.infrastructure.oauth.google.dto

data class GoogleTokenResult(
    val accessToken: String,
    val idToken: String,
    val refreshToken: String?,
)
