package com.yapp.love.infrastructure.oauth.apple

import com.yapp.love.infrastructure.oauth.apple.config.AppleOauthProperties
import com.yapp.love.infrastructure.oauth.apple.dto.AppleIdTokenRequest
import com.yapp.love.infrastructure.oauth.apple.response.AppleTokenResponse
import com.yapp.love.infrastructure.oauth.apple.service.AppleClientSecretService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

private val logger = KotlinLogging.logger {}

@Component
class AppleOauthClient(
    webClientBuilder: WebClient.Builder,
    private val appleProperties: AppleOauthProperties,
    private val clientSecretService: AppleClientSecretService,
) {
    private val webClient: WebClient =
        webClientBuilder
            .baseUrl(appleProperties.aud)
            .build()

    fun exchangeCodeForToken(code: String): AppleTokenResponse {
        val clientSecret = clientSecretService.createClientSecret()

        val formData =
            AppleIdTokenRequest("authorization_code", code, appleProperties.clientId, clientSecret)

        return webClient.post()
            .uri("/auth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(formData.toMultiValueMap()))
            .retrieve()
            .bodyToMono(AppleTokenResponse::class.java)
            .doOnError { e ->
                if (e is WebClientResponseException) {
                    logger.error { "Apple token error: ${e.statusCode} - ${e.responseBodyAsString}" }
                }
            }
            .block() ?: throw IllegalStateException("Apple token response is null")
    }
}
