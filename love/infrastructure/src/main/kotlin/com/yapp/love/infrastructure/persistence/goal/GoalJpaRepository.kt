package com.yapp.love.infrastructure.persistence.goal

import com.yapp.love.domain.goal.model.Goal
import com.yapp.love.domain.goal.repository.GoalRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface GoalJpaRepository : GoalRepository, JpaRepository<Goal, Long> {
    @Query(
        """
        SELECT g FROM Goal g
        WHERE g.coupleId = :coupleId
        AND g.deletedAt IS NULL
        AND g.startDate <= :targetDate
        AND (g.hasEndDate = false OR g.endDate > :targetDate
            OR (g.endDate = :targetDate AND g.goalStatus != com.yapp.love.domain.goal.model.GoalStatus.COMPLETED))
        ORDER BY
            CASE g.repeatCycle
                WHEN 'DAILY' THEN 1
                WHEN 'WEEKLY' THEN 2
                WHEN 'MONTHLY' THEN 3
            END,
            g.startDate ASC,
            g.id ASC
    """,
    )
    override fun findActiveGoalsByCoupleIdAndDate(
        @Param("coupleId") coupleId: Long,
        @Param("targetDate") targetDate: LocalDate,
    ): List<Goal>

    @Query("SELECT g FROM Goal g WHERE g.id = :id AND g.deletedAt IS NULL")
    override fun findActiveGoalById(
        @Param("id") id: Long,
    ): Goal?

    @Modifying
    @Query(
        """
        UPDATE Goal g
        SET g.goalStatus = com.yapp.love.domain.goal.model.GoalStatus.IN_PROGRESS
        WHERE g.goalStatus = com.yapp.love.domain.goal.model.GoalStatus.NOT_STARTED
        AND g.startDate <= :today
        AND g.deletedAt IS NULL
    """,
    )
    override fun updateNotStartedToInProgress(
        @Param("today") today: LocalDate,
    ): Int

    @Modifying
    @Query(
        """
        UPDATE Goal g
        SET g.goalStatus = com.yapp.love.domain.goal.model.GoalStatus.COMPLETED
        WHERE g.goalStatus = com.yapp.love.domain.goal.model.GoalStatus.IN_PROGRESS
        AND g.hasEndDate = true
        AND g.endDate < :today
        AND g.deletedAt IS NULL
    """,
    )
    override fun updateInProgressToCompleted(
        @Param("today") today: LocalDate,
    ): Int

    @Query("SELECT g.id FROM Goal g WHERE g.coupleId = :coupleId")
    override fun findIdsByCoupleId(@Param("coupleId") coupleId: Long): List<Long>

    @Modifying
    @Query("DELETE FROM Goal g WHERE g.coupleId = :coupleId")
    override fun deleteByCoupleId(@Param("coupleId") coupleId: Long)
}
