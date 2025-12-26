package com.yapp.love.infrastructure.oauth.google

import com.yapp.love.domain.auth.adapter.OAuthUserInfo
import com.yapp.love.infrastructure.oauth.google.config.GoogleOAuthProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

private val logger = KotlinLogging.logger {}

@Component
class GoogleIdTokenVerifier(
    webClientBuilder: WebClient.Builder,
    private val googleProperties: GoogleOAuthProperties,
) {
    private val webClient: WebClient =
        webClientBuilder
            .baseUrl(GOOGLE_TOKENINFO_URI)
            .build()

    fun verify(idToken: String): OAuthUserInfo {
        val response =
            webClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .queryParam("id_token", idToken)
                        .build()
                }
                .retrieve()
                .bodyToMono(GoogleTokenInfoResponse::class.java)
                .block() ?: throw IllegalStateException("Google token info response is null")

        // Verify audience matches our client ID
        if (response.aud != googleProperties.clientId) {
            logger.error { "Google ID token audience mismatch: ${response.aud}" }
            throw IllegalStateException("Invalid Google ID token audience")
        }

        return OAuthUserInfo(
            providerId = response.sub,
            email = response.email,
        )
    }

    companion object {
        private const val GOOGLE_TOKENINFO_URI = "https://oauth2.googleapis.com/tokeninfo"
    }
}

private data class GoogleTokenInfoResponse(
    val sub: String,
    val email: String?,
    val name: String?,
    val picture: String?,
    val aud: String,
    val iss: String,
    val exp: Long,
)
