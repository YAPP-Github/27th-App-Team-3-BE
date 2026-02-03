package com.yapp.love.application.onboarding

import com.yapp.love.domain.couple.CoupleInfoRepository
import com.yapp.love.domain.couple.model.CoupleInfo
import com.yapp.love.domain.onboarding.InviteCodeRepository
import com.yapp.love.domain.onboarding.OnboardingInfoRepository
import com.yapp.love.domain.onboarding.model.InviteCodes
import com.yapp.love.domain.onboarding.model.OnboardingStatus
import com.yapp.love.domain.onboarding.model.UserOnboardingInfo
import com.yapp.love.globalutils.exception.GlobalErrorCode
import com.yapp.love.globalutils.exception.GlobalException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.LocalDate

@Service
class OnboardingService(
    private val onboardingInfoRepository: OnboardingInfoRepository,
    private val inviteCodeRepository: InviteCodeRepository,
    private val coupleInfoRepository: CoupleInfoRepository,
) {

    @Transactional
    fun getOnboardingStatus(userId: Long): OnboardingStatus {
        return onboardingInfoRepository.findByUserId(userId)?.status
            ?:onboardingInfoRepository.save(UserOnboardingInfo.create(userId)).status
    }

    @Transactional
    fun getOrCreateInviteCode(userId: Long): String {
        return inviteCodeRepository.findByCreatorId(userId)?.code
            ?: createInviteCode(userId)
    }

    @Transactional
    fun connectCouple(usedUserId: Long, inviteCode: String) {
        val inviteCodes = inviteCodeRepository.findByCode(inviteCode)
            ?: throw GlobalException(GlobalErrorCode.NOT_FOUND, "유효하지 않은 초대 코드입니다.")

        inviteCodes.use(usedUserId)
        inviteCodeRepository.save(inviteCodes)

        coupleInfoRepository.save(
            CoupleInfo.create(
                user1Id = inviteCodes.creatorId,
                user2Id = usedUserId,
                inviteCodeId = inviteCodes.id!!,
            )
        )

        onboardingInfoRepository.save(UserOnboardingInfo.create(usedUserId))
        onboardingInfoRepository.save(UserOnboardingInfo.create(inviteCodes.creatorId))
    }

    @Transactional
    fun setAnniversary(userId: Long, anniversaryDate: LocalDate) {
        val coupleInfo = coupleInfoRepository.findByUserId(userId)
            ?: throw GlobalException(GlobalErrorCode.NOT_FOUND, "커플 정보를 찾을 수 없습니다.")

        coupleInfo.setAnniversary(anniversaryDate)
        coupleInfoRepository.save(coupleInfo)

        updateOnboardingStatus(coupleInfo.user1Id, coupleInfo)
        updateOnboardingStatus(coupleInfo.user2Id, coupleInfo)
    }

    private fun updateOnboardingStatus(userId: Long, coupleInfo: CoupleInfo) {
        val onboardingInfo = onboardingInfoRepository.findByUserId(userId)
            ?: throw GlobalException(GlobalErrorCode.NOT_FOUND, "온보딩 정보를 찾을 수 없습니다.")
        onboardingInfo.updateStatus(coupleInfo)
        onboardingInfoRepository.save(onboardingInfo)
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
