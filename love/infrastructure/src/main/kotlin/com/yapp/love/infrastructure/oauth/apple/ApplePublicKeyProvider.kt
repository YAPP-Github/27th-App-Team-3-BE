package com.yapp.love.infrastructure.oauth.apple

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.net.URI
import java.security.interfaces.RSAPublicKey
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

private val logger = KotlinLogging.logger {}

@Component
class ApplePublicKeyProvider {

    companion object {
        private const val APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys"
        private const val CACHE_TTL_MS = 24 * 60 * 60 * 1000L // 24시간
    }

    private val keyCache = ConcurrentHashMap<String, RSAPublicKey>()
    private val lastFetchTime = AtomicLong(0)

    fun getPublicKey(kid: String): RSAPublicKey {
        // 캐시에 있고 TTL 내라면 캐시 반환
        val cachedKey = keyCache[kid]
        if (cachedKey != null && !isCacheExpired()) {
            return cachedKey
        }

        // 캐시 갱신
        refreshKeys()

        return keyCache[kid]
            ?: throw IllegalStateException("Apple public key not found for kid: $kid")
    }

    private fun isCacheExpired(): Boolean {
        return System.currentTimeMillis() - lastFetchTime.get() > CACHE_TTL_MS
    }

    @Synchronized
    private fun refreshKeys() {
        // Double-check locking
        if (!isCacheExpired() && keyCache.isNotEmpty()) {
            return
        }

        try {
            logger.debug { "Fetching Apple public keys from $APPLE_JWKS_URL" }

            val jwkSet = JWKSet.load(URI(APPLE_JWKS_URL).toURL())

            keyCache.clear()
            jwkSet.keys.forEach { jwk ->
                if (jwk is RSAKey) {
                    keyCache[jwk.keyID] = jwk.toRSAPublicKey()
                }
            }

            lastFetchTime.set(System.currentTimeMillis())
            logger.info { "Apple public keys refreshed. Key IDs: ${keyCache.keys}" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to fetch Apple public keys" }
            // 기존 캐시가 있으면 유지, 없으면 예외
            if (keyCache.isEmpty()) {
                throw IllegalStateException("Failed to load Apple public keys", e)
            }
        }
    }
}