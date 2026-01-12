package com.yapp.love.application.auth.port

interface TokenBlacklistRepository {
    fun add(
        token: String,
        expirationMillis: Long,
    )

    fun exists(token: String): Boolean
}
