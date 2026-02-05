package com.yapp.love.application.auth.dto

data class AppleIdTokenLoginCommand(
    val idToken: String,
    val authorizationCode: String,
)
