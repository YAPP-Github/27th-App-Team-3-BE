package com.yapp.love.infrastructure.persistence.couple

import com.yapp.love.domain.couple.model.CoupleInfo
import com.yapp.love.domain.couple.repository.CoupleRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CoupleJpaRepository : CoupleRepository, JpaRepository<CoupleInfo, Long> {
    @Query("SELECT c FROM CoupleInfo c WHERE c.user1Id = :userId OR c.user2Id = :userId")
    override fun findByUserId(
        @Param("userId") userId: Long,
    ): CoupleInfo?
}
