package com.yapp.love.application.couple

import com.yapp.love.domain.couple.model.CoupleInfo
import com.yapp.love.domain.couple.repository.CoupleRepository
import com.yapp.love.globalutils.exception.GlobalErrorCode
import com.yapp.love.globalutils.exception.GlobalException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CoupleService(
    private val coupleRepository: CoupleRepository,
) {
    fun getCoupleInfoByUserId(userId: Long): CoupleInfo {
        return coupleRepository.findByUserId(userId)
            ?: throw GlobalException(GlobalErrorCode.NOT_FOUND, "커플을 찾을 수 없습니다.")
    }

    fun getPartnerUserIdByCoupleInfo(
        coupleInfo: CoupleInfo,
        myUserId: Long,
    ): Long {
        return if (coupleInfo.user1Id == myUserId) {
            coupleInfo.user2Id
        } else {
            coupleInfo.user1Id
        }
    }

    fun existsById(coupleId: Long): Boolean {
        return coupleRepository.existsById(coupleId)
    }
}
