package com.yapp.love.infrastructure.persistence.couple

import com.yapp.love.domain.couple.CoupleInfoRepository
import com.yapp.love.domain.couple.model.CoupleInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

interface CoupleInfoJpaRepository : JpaRepository<CoupleInfo, Long> {
    @Query("SELECT c FROM CoupleInfo c WHERE c.user1Id = :userId OR c.user2Id = :userId")
    fun findByUser1IdOrUser2Id(userId: Long): CoupleInfo?
}

@Repository
class CoupleInfoRepositoryImpl(
    private val coupleInfoJpaRepository: CoupleInfoJpaRepository
) : CoupleInfoRepository {

    override fun save(coupleInfo: CoupleInfo): CoupleInfo {
        return coupleInfoJpaRepository.save(coupleInfo)
    }

    override fun findById(coupleId: Long): CoupleInfo? {
        return coupleInfoJpaRepository.findById(coupleId).orElse(null)
    }

    override fun findByUserId(userId: Long): CoupleInfo? {
        return coupleInfoJpaRepository.findByUser1IdOrUser2Id(userId)
    }

    override fun deleteById(coupleId: Long) {
        coupleInfoJpaRepository.deleteById(coupleId)
    }
}
