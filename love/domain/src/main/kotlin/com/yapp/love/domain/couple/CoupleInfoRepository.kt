package com.yapp.love.domain.couple

import com.yapp.love.domain.couple.model.CoupleInfo

interface CoupleInfoRepository {
    fun save(coupleInfo: CoupleInfo): CoupleInfo

    fun findById(coupleId: Long): CoupleInfo?

    fun findByUserId(userId: Long): CoupleInfo?
}
