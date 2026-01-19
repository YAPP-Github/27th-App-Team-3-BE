package com.yapp.love.infrastructure.oauth.kakao

import com.yapp.love.application.auth.port.OAuthProvider
import com.yapp.love.application.auth.port.OAuthUserInfo
import com.yapp.love.domain.user.model.SocialProvider
import com.yapp.love.globalutils.exception.GlobalErrorCode
import com.yapp.love.globalutils.exception.GlobalException
import com.yapp.love.infrastructure.oauth.kakao.config.KakaoOAuthProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class KakaoOAuthService(
    private val kakaoOAuthClient: KakaoOAuthClient,
    private val kakaoProperties: KakaoOAuthProperties,
) : OAuthProvider {
    companion object {
        private const val KAKAO_ISSUER = "https://kauth.kakao.com"
    }

    override fun getProviderType(): SocialProvider = SocialProvider.KAKAO

    override fun authenticate(code: String): OAuthUserInfo {
        val tokenResponse = kakaoOAuthClient.exchangeCodeForToken(code)
        val tokenInfo = kakaoOAuthClient.verifyIdToken(tokenResponse.idToken)

        if (tokenInfo.aud != kakaoProperties.clientId) {
            logger.error {
                "Kakao ID token audience mismatch: expected=${kakaoProperties.clientId}, actual=${tokenInfo.aud}"
            }
            throw GlobalException(GlobalErrorCode.INVALID_TOKEN, "카카오 인증 토큰이 유효하지 않습니다.")
        }

        if (tokenInfo.iss != KAKAO_ISSUER) {
            logger.error {
                "Kakao ID token issuer mismatch: expected=$KAKAO_ISSUER, actual=${tokenInfo.iss}"
            }
            throw GlobalException(GlobalErrorCode.INVALID_TOKEN, "카카오 인증 토큰이 유효하지 않습니다.")
        }

        val currentTime = System.currentTimeMillis() / 1000
        if (tokenInfo.exp < currentTime) {
            logger.error { "Kakao ID token expired: exp=${tokenInfo.exp}, current=$currentTime" }
            throw GlobalException(GlobalErrorCode.TOKEN_EXPIRED, "카카오 인증 토큰이 만료되었습니다.")
        }

        return OAuthUserInfo(
            providerId = tokenInfo.sub,
            email = tokenInfo.email,
        )
    }
}
