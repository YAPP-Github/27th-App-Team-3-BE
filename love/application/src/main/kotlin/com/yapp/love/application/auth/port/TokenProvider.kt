package com.yapp.love.application.auth.port

interface TokenProvider {
    fun createAccessToken(userId: Long): String

    fun createRefreshToken(userId: Long): String
}
