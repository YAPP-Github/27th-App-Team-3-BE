package com.yapp.love.infrastructure.persistence

import com.yapp.love.domain.onboarding.OnboardingInfoRepository
import com.yapp.love.domain.onboarding.model.UserOnboardingInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OnboardingInfoJpaRepository : OnboardingInfoRepository, JpaRepository<UserOnboardingInfo, Long>
