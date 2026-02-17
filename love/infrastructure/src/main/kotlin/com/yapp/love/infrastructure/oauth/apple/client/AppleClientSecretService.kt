package com.yapp.love.infrastructure.oauth.apple.client

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.yapp.love.infrastructure.oauth.apple.config.AppleOAuthProperties
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

@Service
class AppleClientSecretService(
    private val appleProperties: AppleOAuthProperties,
    private val keyLoader: ApplePrivateKeyLoader,
) {
    fun createClientSecret(now: Instant = Instant.now()): String {
        val exp = now.plus(5, ChronoUnit.MINUTES)

        val claims =
            JWTClaimsSet.Builder()
                .issuer(appleProperties.teamId)
                .subject(appleProperties.clientId)
                .audience(appleProperties.aud)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .build()

        val header =
            JWSHeader.Builder(JWSAlgorithm.ES256)
                .keyID(appleProperties.keyId)
                .build()

        val signedJwt = SignedJWT(header, claims)
        val signer = ECDSASigner(keyLoader.privateKey)

        signedJwt.sign(signer)
        return signedJwt.serialize()
    }
}