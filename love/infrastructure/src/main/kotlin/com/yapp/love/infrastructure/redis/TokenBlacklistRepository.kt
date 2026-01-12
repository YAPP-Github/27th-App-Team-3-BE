package com.yapp.love.infrastructure.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit
import com.yapp.love.application.auth.port.TokenBlacklistRepository as TokenBlacklistRepositoryPort

@Repository
class TokenBlacklistRepository(
    private val redisTemplate: StringRedisTemplate,
) : TokenBlacklistRepositoryPort {
    companion object {
        private const val KEY_PREFIX = "blacklist:token:"
    }

    override fun add(
        token: String,
        expirationMillis: Long,
    ) {
        val key = "$KEY_PREFIX$token"
        // AccessToken의 남은 만료 시간만큼만 저장
        val ttlSeconds = expirationMillis / 1000
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(key, "1", ttlSeconds, TimeUnit.SECONDS)
        }
    }

    override fun exists(token: String): Boolean {
        val key = "$KEY_PREFIX$token"
        return redisTemplate.hasKey(key) ?: false
    }
}
