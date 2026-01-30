package com.yapp.love.domain.couple.repository

import com.yapp.love.domain.couple.model.CoupleInfo

interface CoupleRepository {
    /**
     * userId가 포함된(user1Id or user2Id) 커플 정보 조회
     */
    fun findByUserId(userId: Long): CoupleInfo?

    /**
     * coupleId로 커플 존재 여부 확인
     */
    fun existsById(coupleId: Long): Boolean
}
