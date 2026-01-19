package com.yapp.love.infrastructure.oauth.kakao

import com.yapp.love.globalutils.exception.GlobalErrorCode
import com.yapp.love.globalutils.exception.GlobalException
import com.yapp.love.infrastructure.oauth.kakao.config.KakaoOAuthProperties
import com.yapp.love.infrastructure.oauth.kakao.response.KakaoTokenInfoResponse
import com.yapp.love.infrastructure.oauth.kakao.response.KakaoTokenResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

private val logger = KotlinLogging.logger {}

@Component
class KakaoOAuthClient(
    private val kakaoProperties: KakaoOAuthProperties,
) {
    private val restClient: RestClient =
        RestClient.builder()
            .baseUrl(KAKAO_TOKEN_URI)
            .build()

    fun exchangeCodeForToken(code: String): KakaoTokenResponse {
        val formData =
            LinkedMultiValueMap<String, String>().apply {
                add("grant_type", "authorization_code")
                add("code", code)
                add("client_id", kakaoProperties.clientId)
                add("client_secret", kakaoProperties.clientSecret)
            }

        return try {
            restClient.post()
                .uri("/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(KakaoTokenResponse::class.java)
                ?: run {
                    logger.error { "Kakao token endpoint returned null response body" }
                    throw GlobalException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "카카오 인증 응답을 처리할 수 없습니다.")
                }
        } catch (e: RestClientResponseException) {
            logger.error { "Kakao token error: ${e.statusCode} - ${e.responseBodyAsString}" }
            when (e.statusCode.value()) {
                429 -> throw GlobalException(GlobalErrorCode.TOO_MANY_REQUESTS, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.")
                401, 403 -> throw GlobalException(GlobalErrorCode.UNAUTHORIZED, "카카오 인증에 실패했습니다.")
                else -> throw GlobalException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "카카오 로그인 처리 중 오류가 발생했습니다.")
            }
        } catch (e: ResourceAccessException) {
            logger.error(e) { "Kakao token exchange failed - network/connection error" }
            throw GlobalException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "카카오 인증 서버 연결에 실패했습니다.")
        } catch (e: Exception) {
            logger.error(e) { "Kakao token exchange failed with unexpected error" }
            throw GlobalException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "카카오 로그인 처리 중 오류가 발생했습니다.")
        }
    }

    fun verifyIdToken(idToken: String): KakaoTokenInfoResponse {
        val formData =
            LinkedMultiValueMap<String, String>().apply {
                add("id_token", idToken)
            }

        return try {
            restClient.post()
                .uri("/tokeninfo")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(KakaoTokenInfoResponse::class.java)
                ?: run {
                    logger.error { "Kakao tokeninfo endpoint returned null response body" }
                    throw GlobalException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "카카오 인증 응답을 처리할 수 없습니다.")
                }
        } catch (e: RestClientResponseException) {
            logger.error { "Kakao token verify error: ${e.statusCode} - ${e.responseBodyAsString}" }
            when (e.statusCode.value()) {
                429 -> throw GlobalException(GlobalErrorCode.TOO_MANY_REQUESTS, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.")
                401, 403 -> throw GlobalException(GlobalErrorCode.INVALID_TOKEN, "카카오 인증 토큰이 유효하지 않습니다.")
                else -> throw GlobalException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "카카오 토큰 검증 중 오류가 발생했습니다.")
            }
        } catch (e: ResourceAccessException) {
            logger.error(e) { "Kakao token verify failed - network/connection error" }
            throw GlobalException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "카카오 인증 서버 연결에 실패했습니다.")
        } catch (e: Exception) {
            logger.error(e) { "Kakao token verify failed with unexpected error" }
            throw GlobalException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "카카오 토큰 검증 중 오류가 발생했습니다.")
        }
    }

    companion object {
        private const val KAKAO_TOKEN_URI = "https://kauth.kakao.com/oauth"
    }
}
