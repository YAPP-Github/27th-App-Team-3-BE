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
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

@Component
class AppleOauthClient(
    webClientBuilder: WebClient.Builder,
    private val appleProperties: AppleOauthProperties,
    private val clientSecretService: AppleClientSecretService,
) {
    companion object {
        private const val APPLE_TOKEN_ENDPOINT = "https://appleid.apple.com"
    }

    private val webClient: WebClient =
        webClientBuilder
            .baseUrl(APPLE_TOKEN_ENDPOINT)
            .build()

    fun exchangeCodeForToken(code: String): AppleTokenResponse {
        val clientSecret = clientSecretService.createClientSecret()

        val formData =
            AppleIdTokenRequest("authorization_code", code, appleProperties.clientId, clientSecret)

        logger.debug {
            "Requesting Apple token exchange with clientId: ${appleProperties.clientId}, code length: ${code.length}"
        }

        return try {
            webClient.post()
                .uri("/auth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData.toMultiValueMap()))
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    response.bodyToMono<String>()
                        .defaultIfEmpty("(empty response body)")
                        .flatMap { body ->
                            logger.error {
                                """
                                |Apple token exchange failed:
                                |Status: ${response.statusCode()}
                                |Response Body: $body
                                |Request URL: ${APPLE_TOKEN_ENDPOINT}/auth/token
                                |Client ID: ${appleProperties.clientId}
                                """.trimMargin()
                            }
                            val statusCode = response.statusCode()
                            Mono.error(
                                WebClientResponseException.create(
                                    statusCode.value(),
                                    "Apple token exchange failed",
                                    response.headers().asHttpHeaders(),
                                    body.toByteArray(),
                                    null,
                                ),
                            )
                        }
                }
                .bodyToMono(AppleTokenResponse::class.java)
                .block() ?: throw IllegalStateException("Apple token response is null")
        } catch (e: WebClientResponseException) {
            // 이미 onStatus에서 로깅했지만, 혹시 모를 경우를 대비
            if (e.message?.contains("Apple token exchange failed") != true) {
                logger.error(e) {
                    """
                    |Apple token exchange failed (caught):
                    |Status: ${e.statusCode}
                    |Response Body: ${e.responseBodyAsString}
                    |Request URL: ${APPLE_TOKEN_ENDPOINT}/auth/token
                    |Client ID: ${appleProperties.clientId}
                    """.trimMargin()
                }
            }
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Apple token exchange failed: ${e.message}" }
            throw e
        }
    }
}
