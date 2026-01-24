package com.yapp.love.application.onboarding

import com.yapp.love.domain.onboarding.OnboardingInfoRepository
import com.yapp.love.domain.onboarding.model.OnboardingStatus
import com.yapp.love.globalutils.exception.GlobalErrorCode
import com.yapp.love.globalutils.exception.GlobalException
import org.springframework.stereotype.Service

@Service
class OnboardingService(
    private val onboardingInfoRepository: OnboardingInfoRepository,
) {

    fun getOnboardingStatus(userId: Long): OnboardingStatus {
        return onboardingInfoRepository.findByUserId(userId)?.status
            ?: throw GlobalException(GlobalErrorCode.NOT_FOUND, "온보딩 정보를 찾을 수 없습니다.")
    }
}
