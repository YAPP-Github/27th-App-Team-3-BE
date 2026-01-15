package com.yapp.love.infrastructure.oauth.google

import com.yapp.love.infrastructure.oauth.google.config.GoogleOAuthProperties
import com.yapp.love.infrastructure.oauth.google.response.GoogleTokenResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

private val logger = KotlinLogging.logger {}

@Component
class GoogleOAuthClient(
    webClientBuilder: WebClient.Builder,
    private val googleProperties: GoogleOAuthProperties,
) {
    private val webClient: WebClient =
        webClientBuilder
            .baseUrl(GOOGLE_TOKEN_URI)
            .build()

    fun exchangeCodeForToken(code: String): GoogleTokenResponse {
        val formData =
            LinkedMultiValueMap<String, String>().apply {
                add("grant_type", "authorization_code")
                add("code", code)
                add("client_id", googleProperties.clientId)
                add("client_secret", googleProperties.clientSecret)
                add("redirect_uri", googleProperties.redirectUri)
            }

        return webClient.post()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .bodyToMono(GoogleTokenResponse::class.java)
            .doOnError { e ->
                if (e is WebClientResponseException) {
                    logger.error { "Google token error: ${e.statusCode} - ${e.responseBodyAsString}" }
                }
            }
            .block() ?: throw IllegalStateException("Google token response is null")
    }

    companion object {
        private const val GOOGLE_TOKEN_URI = "https://oauth2.googleapis.com/token"
    }
}
