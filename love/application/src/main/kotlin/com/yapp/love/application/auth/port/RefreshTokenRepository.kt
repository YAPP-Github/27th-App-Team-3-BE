package com.yapp.love.application.auth.port

interface RefreshTokenRepository {
    fun save(
        userId: Long,
        refreshToken: String,
    )

    fun findByUserId(userId: Long): String?

    fun delete(userId: Long)

    fun exists(
        userId: Long,
        refreshToken: String,
    ): Boolean
}
