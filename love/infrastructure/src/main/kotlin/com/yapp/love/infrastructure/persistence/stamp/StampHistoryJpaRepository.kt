package com.yapp.love.infrastructure.persistence.stamp

import com.yapp.love.domain.stamp.model.StampHistory
import com.yapp.love.domain.stamp.repository.StampHistoryRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface StampHistoryJpaRepository : JpaRepository<StampHistory, Long>, StampHistoryRepository {
    override fun findByPhotologId(photologId: Long): StampHistory?

    @Query(
        """
        SELECT sh FROM StampHistory sh
        WHERE sh.goalId = :goalId
        AND sh.userId = :userId
        AND sh.stampDate BETWEEN :startDate AND :endDate
        ORDER BY sh.stampDate ASC
    """,
    )
    override fun findByGoalIdAndUserIdAndStampDateBetween(
        @Param("goalId") goalId: Long,
        @Param("userId") userId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
    ): List<StampHistory>

    @Query(
        """
        SELECT sh FROM StampHistory sh
        WHERE sh.goalId IN :goalIds
        AND sh.userId IN :userIds
        AND sh.stampDate BETWEEN :startDate AND :endDate
        ORDER BY sh.goalId, sh.userId, sh.stampDate ASC
    """,
    )
    override fun findByGoalIdsAndUserIdsAndStampDateBetween(
        @Param("goalIds") goalIds: List<Long>,
        @Param("userIds") userIds: List<Long>,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
    ): List<StampHistory>

    @Query(
        """
        SELECT COUNT(sh) FROM StampHistory sh
        WHERE sh.goalId = :goalId
        AND sh.userId = :userId
    """,
    )
    override fun countByGoalIdAndUserId(
        @Param("goalId") goalId: Long,
        @Param("userId") userId: Long,
    ): Int

    @Modifying
    @Query("DELETE FROM StampHistory sh WHERE sh.photologId = :photologId")
    override fun deleteByPhotologId(
        @Param("photologId") photologId: Long,
    )
}
