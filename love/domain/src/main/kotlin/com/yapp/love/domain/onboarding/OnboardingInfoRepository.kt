package com.yapp.love.domain.onboarding

import com.yapp.love.domain.onboarding.model.UserOnboardingInfo

interface OnboardingInfoRepository {
    fun findByUserId(userId: Long): UserOnboardingInfo?
}
