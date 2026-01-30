package com.yapp.love.infrastructure.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit
import com.yapp.love.application.auth.port.RefreshTokenRepository as RefreshTokenRepositoryPort

@Repository
class RefreshTokenRepository(
    private val redisTemplate: StringRedisTemplate,
) : RefreshTokenRepositoryPort {
    companion object {
        private const val KEY_PREFIX = "refresh_token:"
        private const val TTL_DAYS = 7L
    }

    override fun save(
        userId: Long,
        refreshToken: String,
    ) {
        val key = "$KEY_PREFIX$userId"
        redisTemplate.opsForValue().set(key, refreshToken, TTL_DAYS, TimeUnit.DAYS)
    }

    override fun findByUserId(userId: Long): String? {
        val key = "$KEY_PREFIX$userId"
        return redisTemplate.opsForValue().get(key)
    }

    override fun delete(userId: Long) {
        val key = "$KEY_PREFIX$userId"
        redisTemplate.delete(key)
    }

    override fun exists(
        userId: Long,
        refreshToken: String,
    ): Boolean {
        val storedToken = findByUserId(userId)
        return storedToken != null && storedToken == refreshToken
    }
}
