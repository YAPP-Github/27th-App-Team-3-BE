package com.yapp.love.infrastructure.oauth.apple.client

import com.yapp.love.infrastructure.oauth.apple.config.AppleKeyProperties
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

@Component
class ApplePrivateKeyLoader(
    private val keyProperties: AppleKeyProperties,
    private val resourceLoader: ResourceLoader,
) {
    val privateKey: ECPrivateKey by lazy { loadPrivateKey() }

    private fun loadPrivateKey(): ECPrivateKey {
        val resource = resourceLoader.getResource(keyProperties.path)
        val pem = resource.inputStream.bufferedReader().use { it.readText() }

        val cleaned =
            pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\s".toRegex(), "")

        val decoded = Base64.getDecoder().decode(cleaned)
        val keySpec = PKCS8EncodedKeySpec(decoded)

        return KeyFactory.getInstance("EC").generatePrivate(keySpec) as ECPrivateKey
    }
}