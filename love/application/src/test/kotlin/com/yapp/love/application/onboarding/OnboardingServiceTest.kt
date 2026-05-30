package com.yapp.love.application.onboarding

import com.yapp.love.domain.couple.CoupleInfoRepository
import com.yapp.love.domain.couple.model.CoupleInfo
import com.yapp.love.domain.onboarding.InviteCodeRepository
import com.yapp.love.domain.onboarding.OnboardingInfoRepository
import com.yapp.love.domain.onboarding.model.OnboardingStatus
import com.yapp.love.domain.onboarding.model.UserOnboardingInfo
import com.yapp.love.domain.user.UserAdditionInfoRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDate

class OnboardingServiceTest : DescribeSpec({

    val inviteCodeRepository = mockk<InviteCodeRepository>()
    val coupleInfoRepository = mockk<CoupleInfoRepository>()
    val userAdditionInfoRepository = mockk<UserAdditionInfoRepository>()
    val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    val userAId = 1L
    val userBId = 2L
    val anniversaryDate = LocalDate.of(2025, 1, 1)

    fun createService(onboardingInfoRepository: OnboardingInfoRepository) =
        OnboardingService(
            onboardingInfoRepository = onboardingInfoRepository,
            inviteCodeRepository = inviteCodeRepository,
            coupleInfoRepository = coupleInfoRepository,
            userAdditionInfoRepository = userAdditionInfoRepository,
            notificationEventPublisher = eventPublisher,
        )

    beforeEach {
        clearAllMocks()
        every { coupleInfoRepository.save(any()) } answers { firstArg() }
        every { userAdditionInfoRepository.save(any()) } answers { firstArg() }
    }

    describe("setAnniversary") {
        it("한 명이 기념일을 설정하면 ANNIVERSARY_SETUP 상태인 양쪽 온보딩이 완료된다") {
            val coupleInfo = createCoupleInfo()
            val userAOnboarding = createOnboardingInfo(userAId, OnboardingStatus.ANNIVERSARY_SETUP)
            val userBOnboarding = createOnboardingInfo(userBId, OnboardingStatus.ANNIVERSARY_SETUP)
            val onboardingInfoRepository = FakeOnboardingInfoRepository(userAOnboarding, userBOnboarding)
            val service = createService(onboardingInfoRepository)
            every { coupleInfoRepository.findByUserId(userAId) } returns coupleInfo

            service.setAnniversary(userAId, anniversaryDate)

            coupleInfo.anniversaryDate shouldBe anniversaryDate
            userAOnboarding.status shouldBe OnboardingStatus.COMPLETED
            userBOnboarding.status shouldBe OnboardingStatus.COMPLETED
            onboardingInfoRepository.savedUserIds shouldBe listOf(userAId, userBId)
        }

        it("상대가 아직 ANNIVERSARY_SETUP이 아니면 기념일 설정 시 강제로 완료하지 않는다") {
            val coupleInfo = createCoupleInfo()
            val userAOnboarding = createOnboardingInfo(userAId, OnboardingStatus.ANNIVERSARY_SETUP)
            val userBOnboarding = createOnboardingInfo(userBId, OnboardingStatus.PROFILE_SETUP)
            val onboardingInfoRepository = FakeOnboardingInfoRepository(userAOnboarding, userBOnboarding)
            val service = createService(onboardingInfoRepository)
            every { coupleInfoRepository.findByUserId(userAId) } returns coupleInfo

            service.setAnniversary(userAId, anniversaryDate)

            userAOnboarding.status shouldBe OnboardingStatus.COMPLETED
            userBOnboarding.status shouldBe OnboardingStatus.PROFILE_SETUP
            onboardingInfoRepository.savedUserIds shouldBe listOf(userAId)
        }

        it("이미 완료된 사용자는 스킵하여 반복 기념일 설정이 예외를 발생시키지 않는다") {
            val coupleInfo = createCoupleInfo()
            val completedOnboarding = createOnboardingInfo(userAId, OnboardingStatus.COMPLETED)
            val waitingOnboarding = createOnboardingInfo(userBId, OnboardingStatus.ANNIVERSARY_SETUP)
            val onboardingInfoRepository = FakeOnboardingInfoRepository(completedOnboarding, waitingOnboarding)
            val service = createService(onboardingInfoRepository)
            every { coupleInfoRepository.findByUserId(userAId) } returns coupleInfo

            service.setAnniversary(userAId, anniversaryDate)

            completedOnboarding.status shouldBe OnboardingStatus.COMPLETED
            waitingOnboarding.status shouldBe OnboardingStatus.COMPLETED
            onboardingInfoRepository.savedUserIds shouldBe listOf(userBId)
        }
    }

    describe("setProfile") {
        it("기념일이 이미 있으면 PROFILE_SETUP 상태 사용자는 프로필 설정 시 완료된다") {
            val coupleInfo = createCoupleInfo(anniversaryDate = anniversaryDate)
            val userBOnboarding = createOnboardingInfo(userBId, OnboardingStatus.PROFILE_SETUP)
            val onboardingInfoRepository = FakeOnboardingInfoRepository(userBOnboarding)
            val service = createService(onboardingInfoRepository)
            every { userAdditionInfoRepository.findByUserId(userBId) } returns null
            every { coupleInfoRepository.findByUserId(userBId) } returns coupleInfo

            service.setProfile(userBId, "keeper")

            userBOnboarding.status shouldBe OnboardingStatus.COMPLETED
            onboardingInfoRepository.savedUserIds shouldBe listOf(userBId)
        }
    }
})

private class FakeOnboardingInfoRepository(
    vararg onboardingInfos: UserOnboardingInfo,
) : OnboardingInfoRepository {
    private val onboardingInfos = onboardingInfos.associateBy { it.userId }.toMutableMap()

    val savedUserIds = mutableListOf<Long>()

    override fun findByUserId(userId: Long): UserOnboardingInfo? = onboardingInfos[userId]

    override fun save(onboardingInfo: UserOnboardingInfo): UserOnboardingInfo {
        onboardingInfos[onboardingInfo.userId] = onboardingInfo
        savedUserIds += onboardingInfo.userId
        return onboardingInfo
    }

    override fun deleteByUserId(userId: Long) {
        onboardingInfos.remove(userId)
    }
}

private fun createCoupleInfo(anniversaryDate: LocalDate? = null) =
    CoupleInfo(
        id = 1L,
        user1Id = 1L,
        user2Id = 2L,
        inviteCodeId = 10L,
        anniversaryDate = anniversaryDate,
    )

private fun createOnboardingInfo(
    userId: Long,
    status: OnboardingStatus,
) = UserOnboardingInfo(
    userId = userId,
    status = status,
)
