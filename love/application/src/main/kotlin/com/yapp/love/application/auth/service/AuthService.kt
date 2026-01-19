package com.yapp.love.application.auth.service

import com.yapp.love.application.auth.dto.AppleLoginCommand
import com.yapp.love.application.auth.dto.GoogleLoginCommand
import com.yapp.love.application.auth.dto.KakaoLoginCommand
import com.yapp.love.application.auth.dto.OAuthLoginResult
import com.yapp.love.application.auth.dto.RefreshTokenCommand
import com.yapp.love.application.auth.dto.TokenRefreshResult
import com.yapp.love.application.auth.port.OAuthProvider
import com.yapp.love.application.auth.port.RefreshTokenRepository
import com.yapp.love.application.auth.port.TokenProvider
import com.yapp.love.domain.user.model.SocialProvider
import com.yapp.love.domain.user.model.User
import com.yapp.love.domain.user.repository.UserRepository
import com.yapp.love.globalutils.exception.GlobalErrorCode
import com.yapp.love.globalutils.exception.GlobalException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

/**
 * 인증 관련 서비스
 *
 * 소셜 로그인, 토큰 갱신, 로그아웃 등 인증 관련 비즈니스 로직을 처리합니다.
 */

private val logger = KotlinLogging.logger {}

@Service
class AuthService(
    oauthProviders: List<OAuthProvider>,
    private val userRepository: UserRepository,
    private val tokenProvider: TokenProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val transactionTemplate: TransactionTemplate,
) {
    private val providerMap: Map<SocialProvider, OAuthProvider> =
        oauthProviders.associateBy { it.getProviderType() }

    fun appleLogin(command: AppleLoginCommand): OAuthLoginResult {
        return login(provider = SocialProvider.APPLE, code = command.code)
    }

    fun googleLogin(command: GoogleLoginCommand): OAuthLoginResult {
        return login(provider = SocialProvider.GOOGLE, code = command.code)
    }

    fun kakaoLogin(command: KakaoLoginCommand): OAuthLoginResult {
        return login(provider = SocialProvider.KAKAO, code = command.code)
    }

    private fun login(
        provider: SocialProvider,
        code: String,
    ): OAuthLoginResult {
        val oauthProvider =
            providerMap[provider]
                ?: throw GlobalException(
                    errorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR,
                    message = "OAuth provider not registered: $provider",
                )

        val userInfo = oauthProvider.authenticate(code)

        val result =
            transactionTemplate.execute {
                val (user, isNewUser) =
                    findOrCreateUser(
                        provider = provider,
                        providerId = userInfo.providerId,
                        email = userInfo.email,
                        name = userInfo.email?.substringBefore("@"),
                    )
                createLoginResult(user, isNewUser)
            }

        return result ?: run {
            logger.error {
                "Transaction returned null during login - provider=$provider, providerId=${userInfo.providerId}"
            }
            throw GlobalException(
                GlobalErrorCode.INTERNAL_SERVER_ERROR,
                "로그인 처리에 실패했습니다. 다시 시도해주세요.",
            )
        }
    }

    private fun findOrCreateUser(
        provider: SocialProvider,
        providerId: String,
        email: String?,
        name: String?,
    ): Pair<User, Boolean> {
        val existingUser =
            userRepository.findByOauthProviderAndOauthProviderId(
                oauthProvider = provider,
                oauthProviderId = providerId,
            )

        if (existingUser != null) {
            return existingUser to false
        }

        val defaultName = name ?: email?.substringBefore("@") ?: "${provider.name} User"
        val defaultEmail = email ?: "$providerId@${provider.name.lowercase()}.private"

        val newUser =
            userRepository.save(
                User(
                    name = defaultName,
                    email = defaultEmail,
                    oauthProvider = provider,
                    oauthProviderId = providerId,
                ),
            )

        return newUser to true
    }

    private fun createLoginResult(
        user: User,
        isNewUser: Boolean,
    ): OAuthLoginResult {
        val accessToken = tokenProvider.createAccessToken(user.id!!)
        val refreshToken = tokenProvider.createRefreshToken(user.id!!)

        refreshTokenRepository.save(user.id!!, refreshToken)

        return OAuthLoginResult(
            userId = user.id!!,
            accessToken = accessToken,
            refreshToken = refreshToken,
            isNewUser = isNewUser,
        )
    }

    /**
     * RefreshToken으로 AccessToken 갱신
     */
    fun refreshToken(command: RefreshTokenCommand): TokenRefreshResult {
        val refreshToken = command.refreshToken

        if (!tokenProvider.validateToken(refreshToken)) {
            throw GlobalException(GlobalErrorCode.INVALID_TOKEN)
        }

        val tokenType = tokenProvider.getTokenType(refreshToken)
        if (tokenType != TokenProvider.TOKEN_TYPE_REFRESH) {
            throw GlobalException(GlobalErrorCode.AUTH_REFRESH_TOKEN_TYPE_MISMATCH)
        }

        val userId = tokenProvider.getUserIdFromToken(refreshToken)

        if (!refreshTokenRepository.exists(userId, refreshToken)) {
            throw GlobalException(GlobalErrorCode.AUTH_REFRESH_TOKEN_REVOKED)
        }

        val newAccessToken = tokenProvider.createAccessToken(userId)
        val newRefreshToken = tokenProvider.createRefreshToken(userId)

        refreshTokenRepository.save(userId, newRefreshToken)

        return TokenRefreshResult(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
        )
    }

    fun logout(userId: Long) {
        refreshTokenRepository.delete(userId)
    }
}
