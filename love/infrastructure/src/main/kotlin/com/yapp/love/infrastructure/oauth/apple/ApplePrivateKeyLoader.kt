package com.yapp.love.infrastructure.oauth.apple

import com.yapp.love.infrastructure.oauth.apple.config.AppleKeyProperties
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

/**
 * Apple AuthKey(.p8) 로더
 *
 * Apple Developer에서 발급받은 .p8 파일을 로드하여 EC KeyPair를 생성합니다.
 * client_secret JWT 서명에 사용됩니다.
 */
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
