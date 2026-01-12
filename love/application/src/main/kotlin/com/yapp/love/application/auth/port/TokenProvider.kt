package com.yapp.love.application.auth.port

interface TokenProvider {
    fun createAccessToken(userId: Long): String

    fun createRefreshToken(userId: Long): String

    fun validateToken(token: String): Boolean

    fun getUserIdFromToken(token: String): Long

    fun getTokenType(token: String): String

    fun getRemainingExpirationTime(token: String): Long

    companion object {
        const val TOKEN_TYPE_ACCESS = "access"
        const val TOKEN_TYPE_REFRESH = "refresh"
    }
}
