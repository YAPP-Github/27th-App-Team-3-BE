package com.yapp.love.application.onboarding

import com.yapp.love.domain.onboarding.InviteCodeRepository
import com.yapp.love.domain.onboarding.OnboardingInfoRepository
import com.yapp.love.domain.onboarding.model.InviteCodes
import com.yapp.love.domain.onboarding.model.OnboardingStatus
import com.yapp.love.globalutils.exception.GlobalErrorCode
import com.yapp.love.globalutils.exception.GlobalException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom

@Service
class OnboardingService(
    private val onboardingInfoRepository: OnboardingInfoRepository,
    private val inviteCodeRepository: InviteCodeRepository,
) {
    fun getOnboardingStatus(userId: Long): OnboardingStatus {
        return onboardingInfoRepository.findByUserId(userId)?.status
            ?: throw GlobalException(GlobalErrorCode.NOT_FOUND, "온보딩 정보를 찾을 수 없습니다.")
    }

    @Transactional
    fun getOrCreateInviteCode(userId: Long): String {
        return inviteCodeRepository.findByCreatorId(userId)?.code
            ?: createInviteCode(userId)
    }

    private fun createInviteCode(userId: Long): String {
        val code = generateUniqueCode()
        inviteCodeRepository.save(InviteCodes(code = code, creatorId = userId))
        return code
    }

    private fun generateUniqueCode(): String {
        return generateSequence { generateRandomCode() }
            .take(MAX_RETRY_COUNT)
            .firstOrNull { !inviteCodeRepository.isExistByCode(it) }
            ?: throw GlobalException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "초대 코드 생성에 실패했습니다.")
    }

    private fun generateRandomCode(): String {
        return (1..CODE_LENGTH)
            .map { CODE_CHARS[random.nextInt(CODE_CHARS.length)] }
            .joinToString("")
    }

    companion object {
        private const val CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        private const val CODE_LENGTH = 8
        private const val MAX_RETRY_COUNT = 10
        private val random = SecureRandom()
    }
}
