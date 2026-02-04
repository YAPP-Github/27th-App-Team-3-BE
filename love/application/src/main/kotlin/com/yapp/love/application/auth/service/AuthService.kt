package com.yapp.love.application.auth.service

import com.yapp.love.application.auth.dto.AppleIdTokenLoginCommand
import com.yapp.love.application.auth.dto.GoogleIdTokenLoginCommand
import com.yapp.love.application.auth.dto.OAuthLoginResult
import com.yapp.love.application.auth.dto.RefreshTokenCommand
import com.yapp.love.application.auth.dto.TokenRefreshResult
import com.yapp.love.application.auth.port.OAuthProvider
import com.yapp.love.application.auth.port.OAuthUserInfo
import com.yapp.love.application.auth.port.RefreshTokenRepository
import com.yapp.love.application.auth.port.SocialRefreshTokenProvider
import com.yapp.love.application.auth.port.TokenProvider
import com.yapp.love.domain.couple.CoupleInfoRepository
import com.yapp.love.domain.goal.repository.GoalRepository
import com.yapp.love.domain.onboarding.InviteCodeRepository
import com.yapp.love.domain.onboarding.OnboardingInfoRepository
import com.yapp.love.domain.photolog.repository.PhotologRepository
import com.yapp.love.domain.user.UserAdditionInfoRepository
import com.yapp.love.domain.user.model.SocialProvider
import com.yapp.love.domain.user.model.SocialToken
import com.yapp.love.domain.user.model.User
import com.yapp.love.domain.user.repository.SocialTokenRepository
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
    socialRefreshTokenProviders: List<SocialRefreshTokenProvider>,
    private val userRepository: UserRepository,
    private val tokenProvider: TokenProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val socialTokenRepository: SocialTokenRepository,
    private val coupleInfoRepository: CoupleInfoRepository,
    private val goalRepository: GoalRepository,
    private val photologRepository: PhotologRepository,
    private val onboardingInfoRepository: OnboardingInfoRepository,
    private val inviteCodeRepository: InviteCodeRepository,
    private val userAdditionInfoRepository: UserAdditionInfoRepository,
    private val transactionTemplate: TransactionTemplate,
) {
    private val providerMap: Map<SocialProvider, OAuthProvider> =
        oauthProviders.associateBy { it.getProviderType() }

    private val refreshTokenProviderMap: Map<SocialProvider, SocialRefreshTokenProvider> =
        socialRefreshTokenProviders.associateBy { it.getProviderType() }

    fun appleLoginWithIdToken(command: AppleIdTokenLoginCommand): OAuthLoginResult {
        return loginWithIdToken(
            provider = SocialProvider.APPLE,
            idToken = command.idToken,
            authorizationCode = command.authorizationCode,
        )
    }

    fun googleLoginWithIdToken(command: GoogleIdTokenLoginCommand): OAuthLoginResult {
        return loginWithIdToken(provider = SocialProvider.GOOGLE, idToken = command.idToken)
    }

    private fun loginWithIdToken(
        provider: SocialProvider,
        idToken: String,
        authorizationCode: String? = null,
    ): OAuthLoginResult {
        val oauthProvider = getOAuthProvider(provider)
        var userInfo = oauthProvider.authenticateWithIdToken(idToken)

        // authorizationCode가 있으면 refresh_token 획득 (Apple용)
        authorizationCode?.let { code ->
            val socialRefreshToken = refreshTokenProviderMap[provider]
                ?.exchangeCodeForRefreshToken(code)
            userInfo = userInfo.copy(socialRefreshToken = socialRefreshToken)
        }

        return processLogin(provider, userInfo)
    }

    private fun getOAuthProvider(provider: SocialProvider): OAuthProvider {
        return providerMap[provider]
            ?: throw GlobalException(
                errorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR,
                customMessage = "등록되지 않은 OAuth 프로바이더입니다: $provider",
            )
    }

    private fun processLogin(
        provider: SocialProvider,
        userInfo: OAuthUserInfo,
    ): OAuthLoginResult {
        val result =
            transactionTemplate.execute {
                val (user, isNewUser) =
                    findOrCreateUser(
                        provider = provider,
                        providerId = userInfo.providerId,
                        email = userInfo.email,
                        name = userInfo.email?.substringBefore("@"),
                    )

                // 소셜 refresh token이 있으면 저장 (회원탈퇴 시 토큰 revoke용)
                userInfo.socialRefreshToken?.let { socialRefreshToken ->
                    saveSocialToken(user.id!!, provider, socialRefreshToken)
                }

                createLoginResult(user, isNewUser)
            }

        return result ?: run {
            logger.error { "failed to login user: $provider" }
            throw GlobalException(GlobalErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    private fun saveSocialToken(
        userId: Long,
        provider: SocialProvider,
        refreshToken: String,
    ) {
        val existingToken = socialTokenRepository.findByUserIdAndProvider(userId, provider)
        if (existingToken != null) {
            existingToken.updateRefreshToken(refreshToken)
            socialTokenRepository.save(existingToken)
        } else {
            socialTokenRepository.save(
                SocialToken.create(
                    userId = userId,
                    provider = provider,
                    refreshToken = refreshToken,
                )
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

    /**
     * 회원탈퇴
     *
     * 1. Apple 토큰 revoke (App Store 정책 필수)
     * 2. 커플 관계가 있으면 커플/목표/포토로그 모두 삭제
     * 3. 사용자 관련 데이터 삭제
     */
    fun withdraw(userId: Long) {
        val user = userRepository.findById(userId)
            ?: throw GlobalException(GlobalErrorCode.NOT_FOUND, "존재하지 않는 유저입니다.")

        // 1. Apple 소셜 토큰 revoke (App Store 정책 필수)
        if (user.oauthProvider == SocialProvider.APPLE) {
            val socialToken = socialTokenRepository.findByUserIdAndProvider(userId, SocialProvider.APPLE)
            if (socialToken != null) {
                try {
                    refreshTokenProviderMap[SocialProvider.APPLE]?.revokeToken(socialToken.refreshToken)
                } catch (e: Exception) {
                    logger.error(e) { "Failed to revoke Apple token for user $userId" }
                }
            }
        }

        // 2. 커플 관계 확인 및 관련 데이터 삭제
        val coupleInfo = coupleInfoRepository.findByUserId(userId)

        transactionTemplate.execute {
            if (coupleInfo != null) {
                val coupleId = coupleInfo.id!!
                // 포토로그 삭제 (goal FK 때문에 먼저)
                val goalIds = goalRepository.findIdsByCoupleId(coupleId)
                if (goalIds.isNotEmpty()) {
                    photologRepository.deleteByGoalIdIn(goalIds)
                }
                goalRepository.deleteByCoupleId(coupleId)
                coupleInfoRepository.deleteById(coupleId)
            }

            // 3. 사용자 관련 데이터 삭제 (FK 순서 고려)
            inviteCodeRepository.deleteByCreatorId(userId)
            userAdditionInfoRepository.deleteByUserId(userId)
            onboardingInfoRepository.deleteByUserId(userId)
            socialTokenRepository.deleteByUserId(userId)
            refreshTokenRepository.delete(userId)
            userRepository.deleteById(userId)
        }

        logger.info { "User $userId withdrew successfully" }
    }
}
