package com.yapp.love.infrastructure.oauth.kakao

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
class KakaoPublicKeyProvider {
    companion object {
        private const val KAKAO_JWKS_URL = "https://kauth.kakao.com/.well-known/jwks.json"
        private const val CACHE_TTL_MS = 24 * 60 * 60 * 1000L // 24시간
    }

    private val keyCache = ConcurrentHashMap<String, RSAPublicKey>()
    private val lastFetchTime = AtomicLong(0)

    fun getPublicKey(kid: String): RSAPublicKey {
        val cachedKey = keyCache[kid]
        if (cachedKey != null && !isCacheExpired()) {
            return cachedKey
        }

        refreshKeys()

        return keyCache[kid]
            ?: throw IllegalStateException("Kakao public key not found for kid: $kid")
    }

    private fun isCacheExpired(): Boolean {
        return System.currentTimeMillis() - lastFetchTime.get() > CACHE_TTL_MS
    }

    @Synchronized
    private fun refreshKeys() {
        if (!isCacheExpired() && keyCache.isNotEmpty()) {
            return
        }

        try {
            logger.debug { "Fetching Kakao public keys from $KAKAO_JWKS_URL" }

            val jwkSet = JWKSet.load(URI(KAKAO_JWKS_URL).toURL())

            keyCache.clear()
            jwkSet.keys.forEach { jwk ->
                if (jwk is RSAKey) {
                    keyCache[jwk.keyID] = jwk.toRSAPublicKey()
                }
            }

            lastFetchTime.set(System.currentTimeMillis())
            logger.info { "Kakao public keys refreshed. Key IDs: ${keyCache.keys}" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to fetch Kakao public keys" }
            if (keyCache.isEmpty()) {
                throw IllegalStateException("Failed to load Kakao public keys", e)
            }
        }
    }
}
