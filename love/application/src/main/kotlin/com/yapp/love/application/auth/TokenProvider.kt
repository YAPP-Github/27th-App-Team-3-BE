package com.yapp.love.application.auth

interface TokenProvider {
    fun createAccessToken(userId: Long): String

    fun createRefreshToken(userId: Long): String
}
