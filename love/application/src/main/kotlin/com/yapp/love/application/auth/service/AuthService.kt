package com.yapp.love.application.auth.service

import com.yapp.love.application.auth.OAuthProvider
import com.yapp.love.application.auth.TokenProvider
import com.yapp.love.application.auth.dto.AppleLoginCommand
import com.yapp.love.application.auth.dto.GoogleLoginCommand
import com.yapp.love.application.auth.dto.OAuthLoginResult
import com.yapp.love.domain.user.model.SocialProvider
import com.yapp.love.domain.user.model.User
import com.yapp.love.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 인증 관련 서비스
 *
 * 소셜 로그인, 토큰 갱신, 로그아웃 등 인증 관련 비즈니스 로직을 처리합니다.
 */
@Service
class AuthService(
    oauthProviders: List<OAuthProvider>,
    private val userRepository: UserRepository,
    private val tokenProvider: TokenProvider,
) {
    private val providerMap: Map<SocialProvider, OAuthProvider> =
        oauthProviders.associateBy { it.getProviderType() }

    @Transactional
    fun appleLogin(command: AppleLoginCommand): OAuthLoginResult {
        return login(provider = SocialProvider.APPLE, code = command.code)
    }

    @Transactional
    fun googleLogin(command: GoogleLoginCommand): OAuthLoginResult {
        return login(provider = SocialProvider.GOOGLE, code = command.code)
    }

    private fun login(
        provider: SocialProvider,
        code: String,
    ): OAuthLoginResult {
        val oauthProvider =
            providerMap[provider]
                ?: throw IllegalStateException("OAuth provider not registered: $provider")

        val userInfo = oauthProvider.authenticate(code)

        val (user, isNewUser) =
            findOrCreateUser(
                provider = provider,
                providerId = userInfo.providerId,
                email = userInfo.email,
                name = userInfo.email?.substringBefore("@"),
            )

        return createLoginResult(user, isNewUser)
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

        return OAuthLoginResult(
            userId = user.id!!,
            accessToken = accessToken,
            refreshToken = refreshToken,
            isNewUser = isNewUser,
        )
    }
}
