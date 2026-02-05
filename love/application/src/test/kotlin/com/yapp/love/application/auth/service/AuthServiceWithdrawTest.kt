package com.yapp.love.application.auth.service

import com.yapp.love.application.auth.port.RefreshTokenRepository
import com.yapp.love.application.auth.port.SocialRefreshTokenProvider
import com.yapp.love.application.auth.port.TokenProvider
import com.yapp.love.domain.couple.CoupleInfoRepository
import com.yapp.love.domain.couple.model.CoupleInfo
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
import com.yapp.love.globalutils.exception.GlobalException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate

class AuthServiceWithdrawTest : DescribeSpec({

    lateinit var userRepository: UserRepository
    lateinit var tokenProvider: TokenProvider
    lateinit var refreshTokenRepository: RefreshTokenRepository
    lateinit var socialTokenRepository: SocialTokenRepository
    lateinit var coupleInfoRepository: CoupleInfoRepository
    lateinit var goalRepository: GoalRepository
    lateinit var photologRepository: PhotologRepository
    lateinit var onboardingInfoRepository: OnboardingInfoRepository
    lateinit var inviteCodeRepository: InviteCodeRepository
    lateinit var userAdditionInfoRepository: UserAdditionInfoRepository
    lateinit var transactionTemplate: TransactionTemplate
    lateinit var appleRefreshTokenProvider: SocialRefreshTokenProvider
    lateinit var authService: AuthService

    beforeEach {
        userRepository = mockk()
        tokenProvider = mockk()
        refreshTokenRepository = mockk()
        socialTokenRepository = mockk()
        coupleInfoRepository = mockk()
        goalRepository = mockk()
        photologRepository = mockk()
        onboardingInfoRepository = mockk()
        inviteCodeRepository = mockk()
        userAdditionInfoRepository = mockk()
        transactionTemplate = mockk()
        appleRefreshTokenProvider = mockk()

        every { appleRefreshTokenProvider.getProviderType() } returns SocialProvider.APPLE

        authService = AuthService(
            oauthProviders = emptyList(),
            socialRefreshTokenProviders = listOf(appleRefreshTokenProvider),
            userRepository = userRepository,
            tokenProvider = tokenProvider,
            refreshTokenRepository = refreshTokenRepository,
            socialTokenRepository = socialTokenRepository,
            coupleInfoRepository = coupleInfoRepository,
            goalRepository = goalRepository,
            photologRepository = photologRepository,
            onboardingInfoRepository = onboardingInfoRepository,
            inviteCodeRepository = inviteCodeRepository,
            userAdditionInfoRepository = userAdditionInfoRepository,
            transactionTemplate = transactionTemplate,
        )
    }

    describe("withdraw - 회원탈퇴") {

        val userId = 1L
        val coupleId = 100L
        val partnerId = 2L

        context("존재하지 않는 유저") {
            it("예외가 발생해야 함") {
                // given
                every { userRepository.findById(userId) } returns null

                // when & then
                val exception = shouldThrow<GlobalException> {
                    authService.withdraw(userId)
                }

                exception.getCustomMessage() shouldBe "존재하지 않는 유저입니다."
            }
        }

        context("Apple 유저 탈퇴") {
            it("Apple 토큰을 revoke해야 함") {
                // given
                val user = User(
                    id = userId,
                    name = "테스트",
                    email = "test@apple.com",
                    oauthProvider = SocialProvider.APPLE,
                    oauthProviderId = "apple_123",
                )
                val socialToken = SocialToken(
                    id = 1L,
                    userId = userId,
                    provider = SocialProvider.APPLE,
                    refreshToken = "apple_refresh_token",
                )

                every { userRepository.findById(userId) } returns user
                every { socialTokenRepository.findByUserIdAndProvider(userId, SocialProvider.APPLE) } returns socialToken
                every { appleRefreshTokenProvider.revokeToken("apple_refresh_token") } just Runs
                every { coupleInfoRepository.findByUserId(userId) } returns null
                every { transactionTemplate.execute<Unit>(any()) } answers {
                    firstArg<org.springframework.transaction.support.TransactionCallback<Unit>>()
                        .doInTransaction(mockk())
                }
                every { inviteCodeRepository.deleteByCreatorId(userId) } just Runs
                every { userAdditionInfoRepository.deleteByUserId(userId) } just Runs
                every { onboardingInfoRepository.deleteByUserId(userId) } just Runs
                every { socialTokenRepository.deleteByUserId(userId) } just Runs
                every { refreshTokenRepository.delete(userId) } just Runs
                every { userRepository.deleteById(userId) } just Runs

                // when
                authService.withdraw(userId)

                // then
                verify(exactly = 1) { appleRefreshTokenProvider.revokeToken("apple_refresh_token") }
            }

            it("Apple 토큰 revoke 실패해도 탈퇴는 진행되어야 함") {
                // given
                val user = User(
                    id = userId,
                    name = "테스트",
                    email = "test@apple.com",
                    oauthProvider = SocialProvider.APPLE,
                    oauthProviderId = "apple_123",
                )
                val socialToken = SocialToken(
                    id = 1L,
                    userId = userId,
                    provider = SocialProvider.APPLE,
                    refreshToken = "apple_refresh_token",
                )

                every { userRepository.findById(userId) } returns user
                every { socialTokenRepository.findByUserIdAndProvider(userId, SocialProvider.APPLE) } returns socialToken
                every { appleRefreshTokenProvider.revokeToken("apple_refresh_token") } throws RuntimeException("Apple API Error")
                every { coupleInfoRepository.findByUserId(userId) } returns null
                every { transactionTemplate.execute<Unit>(any()) } answers {
                    firstArg<org.springframework.transaction.support.TransactionCallback<Unit>>()
                        .doInTransaction(mockk())
                }
                every { inviteCodeRepository.deleteByCreatorId(userId) } just Runs
                every { userAdditionInfoRepository.deleteByUserId(userId) } just Runs
                every { onboardingInfoRepository.deleteByUserId(userId) } just Runs
                every { socialTokenRepository.deleteByUserId(userId) } just Runs
                every { refreshTokenRepository.delete(userId) } just Runs
                every { userRepository.deleteById(userId) } just Runs

                // when
                authService.withdraw(userId)

                // then - 유저 삭제가 호출되어야 함
                verify(exactly = 1) { userRepository.deleteById(userId) }
            }
        }

        context("Google 유저 탈퇴") {
            it("토큰 revoke를 시도하지 않아야 함") {
                // given
                val user = User(
                    id = userId,
                    name = "테스트",
                    email = "test@google.com",
                    oauthProvider = SocialProvider.GOOGLE,
                    oauthProviderId = "google_123",
                )

                every { userRepository.findById(userId) } returns user
                every { coupleInfoRepository.findByUserId(userId) } returns null
                every { transactionTemplate.execute<Unit>(any()) } answers {
                    firstArg<org.springframework.transaction.support.TransactionCallback<Unit>>()
                        .doInTransaction(mockk())
                }
                every { inviteCodeRepository.deleteByCreatorId(userId) } just Runs
                every { userAdditionInfoRepository.deleteByUserId(userId) } just Runs
                every { onboardingInfoRepository.deleteByUserId(userId) } just Runs
                every { socialTokenRepository.deleteByUserId(userId) } just Runs
                every { refreshTokenRepository.delete(userId) } just Runs
                every { userRepository.deleteById(userId) } just Runs

                // when
                authService.withdraw(userId)

                // then
                verify(exactly = 0) { appleRefreshTokenProvider.revokeToken(any()) }
                verify(exactly = 1) { userRepository.deleteById(userId) }
            }
        }

        context("커플 관계가 있는 유저 탈퇴") {
            it("커플, 목표, 포토로그가 모두 삭제되어야 함") {
                // given
                val user = User(
                    id = userId,
                    name = "테스트",
                    email = "test@google.com",
                    oauthProvider = SocialProvider.GOOGLE,
                    oauthProviderId = "google_123",
                )
                val coupleInfo = CoupleInfo(
                    id = coupleId,
                    user1Id = userId,
                    user2Id = partnerId,
                    inviteCodeId = 1L,
                    anniversaryDate = LocalDate.of(2024, 1, 1),
                )
                val goalIds = listOf(1L, 2L, 3L)

                every { userRepository.findById(userId) } returns user
                every { coupleInfoRepository.findByUserId(userId) } returns coupleInfo
                every { goalRepository.findIdsByCoupleId(coupleId) } returns goalIds
                every { photologRepository.deleteByGoalIdIn(goalIds) } just Runs
                every { goalRepository.deleteByCoupleId(coupleId) } just Runs
                every { coupleInfoRepository.deleteById(coupleId) } just Runs
                every { transactionTemplate.execute<Unit>(any()) } answers {
                    firstArg<org.springframework.transaction.support.TransactionCallback<Unit>>()
                        .doInTransaction(mockk())
                }
                every { inviteCodeRepository.deleteByCreatorId(userId) } just Runs
                every { userAdditionInfoRepository.deleteByUserId(userId) } just Runs
                every { onboardingInfoRepository.deleteByUserId(userId) } just Runs
                every { socialTokenRepository.deleteByUserId(userId) } just Runs
                every { refreshTokenRepository.delete(userId) } just Runs
                every { userRepository.deleteById(userId) } just Runs

                // when
                authService.withdraw(userId)

                // then
                verifyOrder {
                    photologRepository.deleteByGoalIdIn(goalIds)
                    goalRepository.deleteByCoupleId(coupleId)
                    coupleInfoRepository.deleteById(coupleId)
                    userRepository.deleteById(userId)
                }
            }

            it("목표가 없으면 포토로그 삭제를 시도하지 않아야 함") {
                // given
                val user = User(
                    id = userId,
                    name = "테스트",
                    email = "test@google.com",
                    oauthProvider = SocialProvider.GOOGLE,
                    oauthProviderId = "google_123",
                )
                val coupleInfo = CoupleInfo(
                    id = coupleId,
                    user1Id = userId,
                    user2Id = partnerId,
                    inviteCodeId = 1L,
                    anniversaryDate = LocalDate.of(2024, 1, 1),
                )

                every { userRepository.findById(userId) } returns user
                every { coupleInfoRepository.findByUserId(userId) } returns coupleInfo
                every { goalRepository.findIdsByCoupleId(coupleId) } returns emptyList()
                every { goalRepository.deleteByCoupleId(coupleId) } just Runs
                every { coupleInfoRepository.deleteById(coupleId) } just Runs
                every { transactionTemplate.execute<Unit>(any()) } answers {
                    firstArg<org.springframework.transaction.support.TransactionCallback<Unit>>()
                        .doInTransaction(mockk())
                }
                every { inviteCodeRepository.deleteByCreatorId(userId) } just Runs
                every { userAdditionInfoRepository.deleteByUserId(userId) } just Runs
                every { onboardingInfoRepository.deleteByUserId(userId) } just Runs
                every { socialTokenRepository.deleteByUserId(userId) } just Runs
                every { refreshTokenRepository.delete(userId) } just Runs
                every { userRepository.deleteById(userId) } just Runs

                // when
                authService.withdraw(userId)

                // then
                verify(exactly = 0) { photologRepository.deleteByGoalIdIn(any()) }
                verify(exactly = 1) { coupleInfoRepository.deleteById(coupleId) }
            }
        }

        context("커플 관계가 없는 유저 탈퇴") {
            it("커플 관련 삭제를 시도하지 않아야 함") {
                // given
                val user = User(
                    id = userId,
                    name = "테스트",
                    email = "test@google.com",
                    oauthProvider = SocialProvider.GOOGLE,
                    oauthProviderId = "google_123",
                )

                every { userRepository.findById(userId) } returns user
                every { coupleInfoRepository.findByUserId(userId) } returns null
                every { transactionTemplate.execute<Unit>(any()) } answers {
                    firstArg<org.springframework.transaction.support.TransactionCallback<Unit>>()
                        .doInTransaction(mockk())
                }
                every { inviteCodeRepository.deleteByCreatorId(userId) } just Runs
                every { userAdditionInfoRepository.deleteByUserId(userId) } just Runs
                every { onboardingInfoRepository.deleteByUserId(userId) } just Runs
                every { socialTokenRepository.deleteByUserId(userId) } just Runs
                every { refreshTokenRepository.delete(userId) } just Runs
                every { userRepository.deleteById(userId) } just Runs

                // when
                authService.withdraw(userId)

                // then
                verify(exactly = 0) { goalRepository.findIdsByCoupleId(any()) }
                verify(exactly = 0) { photologRepository.deleteByGoalIdIn(any()) }
                verify(exactly = 0) { goalRepository.deleteByCoupleId(any()) }
                verify(exactly = 0) { coupleInfoRepository.deleteById(any()) }
                verify(exactly = 1) { userRepository.deleteById(userId) }
            }
        }

        context("모든 사용자 데이터 삭제") {
            it("사용자 관련 모든 테이블 데이터가 삭제되어야 함") {
                // given
                val user = User(
                    id = userId,
                    name = "테스트",
                    email = "test@google.com",
                    oauthProvider = SocialProvider.GOOGLE,
                    oauthProviderId = "google_123",
                )

                every { userRepository.findById(userId) } returns user
                every { coupleInfoRepository.findByUserId(userId) } returns null
                every { transactionTemplate.execute<Unit>(any()) } answers {
                    firstArg<org.springframework.transaction.support.TransactionCallback<Unit>>()
                        .doInTransaction(mockk())
                }
                every { inviteCodeRepository.deleteByCreatorId(userId) } just Runs
                every { userAdditionInfoRepository.deleteByUserId(userId) } just Runs
                every { onboardingInfoRepository.deleteByUserId(userId) } just Runs
                every { socialTokenRepository.deleteByUserId(userId) } just Runs
                every { refreshTokenRepository.delete(userId) } just Runs
                every { userRepository.deleteById(userId) } just Runs

                // when
                authService.withdraw(userId)

                // then
                verify(exactly = 1) { inviteCodeRepository.deleteByCreatorId(userId) }
                verify(exactly = 1) { userAdditionInfoRepository.deleteByUserId(userId) }
                verify(exactly = 1) { onboardingInfoRepository.deleteByUserId(userId) }
                verify(exactly = 1) { socialTokenRepository.deleteByUserId(userId) }
                verify(exactly = 1) { refreshTokenRepository.delete(userId) }
                verify(exactly = 1) { userRepository.deleteById(userId) }
            }
        }
    }
})
