package com.yapp.love.infrastructure.oauth.apple.client

import com.yapp.love.infrastructure.oauth.apple.config.AppleOAuthProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

private val logger = KotlinLogging.logger {}

@Component
class AppleOAuthClient(
    private val appleProperties: AppleOAuthProperties,
    private val clientSecretService: AppleClientSecretService,
    restClientBuilder: RestClient.Builder,
) {
    companion object {
        private const val APPLE_TOKEN_ENDPOINT = "https://appleid.apple.com"
    }

    private val restClient: RestClient =
        restClientBuilder.clone()
            .baseUrl(APPLE_TOKEN_ENDPOINT)
            .build()

    fun exchangeCodeForToken(code: String): AppleTokenResponse {
        val clientSecret = clientSecretService.createClientSecret()

        val formData = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("code", code)
            add("client_id", appleProperties.clientId)
            add("client_secret", clientSecret)
        }

        logger.debug {
            "Requesting Apple token exchange with clientId: ${appleProperties.clientId}, code length: ${code.length}"
        }

        return try {
            restClient.post()
                .uri("/auth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(AppleTokenResponse::class.java)
                ?: throw IllegalStateException("Apple token response is null")
        } catch (e: RestClientResponseException) {
            logger.error {
                """
                |Apple token exchange failed:
                |Status: ${e.statusCode}
                |Response Body: ${e.responseBodyAsString}
                |Request URL: ${APPLE_TOKEN_ENDPOINT}/auth/token
                |Client ID: ${appleProperties.clientId}
                """.trimMargin()
            }
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Apple token exchange failed: ${e.message}" }
            throw e
        }
    }

    fun revokeToken(refreshToken: String) {
        val clientSecret = clientSecretService.createClientSecret()

        logger.debug { "Requesting Apple token revocation" }

        val formData = LinkedMultiValueMap<String, String>().apply {
            add("client_id", appleProperties.clientId)
            add("client_secret", clientSecret)
            add("token", refreshToken)
            add("token_type_hint", "refresh_token")
        }

        try {
            restClient.post()
                .uri("/auth/revoke")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .toBodilessEntity()

            logger.info { "Apple token revoked successfully" }
        } catch (e: RestClientResponseException) {
            logger.error {
                """
                |Apple token revocation failed:
                |Status: ${e.statusCode}
                |Response Body: ${e.responseBodyAsString}
                """.trimMargin()
            }
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Apple token revocation failed: ${e.message}" }
            throw e
        }
    }
}