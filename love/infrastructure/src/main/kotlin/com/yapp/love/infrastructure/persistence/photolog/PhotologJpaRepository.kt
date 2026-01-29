package com.yapp.love.infrastructure.persistence.photolog

import com.yapp.love.domain.photolog.model.Photolog
import com.yapp.love.domain.photolog.repository.PhotologRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface PhotologJpaRepository : PhotologRepository, JpaRepository<Photolog, Long> {

    @Query("""
        SELECT p FROM Photolog p
        WHERE p.goalId IN :goalIds
        AND p.verificationDate = :verificationDate
        AND p.status = true
    """)
    override fun findByGoalIdsAndVerificationDate(
        @Param("goalIds") goalIds: List<Long>,
        @Param("verificationDate") verificationDate: LocalDate
    ): List<Photolog>

    @Query("""
        SELECT p FROM Photolog p
        WHERE p.goalId = :goalId
        AND p.userId = :userId
        AND p.verificationDate = :verificationDate
        AND p.status = true
    """)
    override fun findByGoalIdAndUserIdAndVerificationDate(
        @Param("goalId") goalId: Long,
        @Param("userId") userId: Long,
        @Param("verificationDate") verificationDate: LocalDate
    ): Photolog?

    @Modifying
    @Query("""
        UPDATE Photolog p
        SET p.status = false
        WHERE p.goalId = :goalId
        AND p.verificationDate BETWEEN :startDate AND :endDate
        AND p.status = true
    """)
    override fun deleteByGoalIdAndVerificationDateBetween(
        @Param("goalId") goalId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): Int
}
