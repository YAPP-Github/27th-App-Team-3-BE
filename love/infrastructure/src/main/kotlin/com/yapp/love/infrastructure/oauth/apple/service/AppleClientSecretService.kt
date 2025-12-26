package com.yapp.love.infrastructure.oauth.apple.service

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.yapp.love.infrastructure.oauth.apple.ApplePrivateKeyLoader
import com.yapp.love.infrastructure.oauth.apple.config.AppleOauthProperties
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

/**
 * Apple client_secret JWT 생성 서비스
 *
 * Apple /auth/token API 호출 시 필요한 client_secret을 생성합니다.
 * ES256 알고리즘으로 서명된 JWT 형식이며, 유효시간은 5분입니다.
 */
@Service
class AppleClientSecretService(
    private val appleProperties: AppleOauthProperties,
    private val keyLoader: ApplePrivateKeyLoader,
) {
    fun createClientSecret(now: Instant = Instant.now()): String {
        val exp = now.plus(5, ChronoUnit.MINUTES)

        val claims =
            com.nimbusds.jwt.JWTClaimsSet.Builder()
                .issuer(appleProperties.teamId)
                .subject(appleProperties.clientId)
                .audience("https://appleid.apple.com")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .build()

        val header =
            JWSHeader.Builder(JWSAlgorithm.ES256)
                .keyID(appleProperties.keyId)
                .build()

        val signedJwt = com.nimbusds.jwt.SignedJWT(header, claims)
        val signer = com.nimbusds.jose.crypto.ECDSASigner(keyLoader.privateKey)

        signedJwt.sign(signer)
        return signedJwt.serialize()
    }
}
