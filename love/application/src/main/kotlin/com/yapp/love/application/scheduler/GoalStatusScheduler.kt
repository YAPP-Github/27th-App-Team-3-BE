package com.yapp.love.application.scheduler

import com.yapp.love.domain.goal.repository.GoalRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
class GoalStatusScheduler(
    private val goalRepository: GoalRepository,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 매일 자정에 목표 상태 업데이트
     * - NOT_STARTED -> IN_PROGRESS (시작일이 오늘 이전인 목표)
     * - IN_PROGRESS -> COMPLETED (종료일이 오늘 이전인 목표)
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    fun updateGoalStatuses() {
        val today = LocalDate.now()

        val startedCount = goalRepository.updateNotStartedToInProgress(today)
        val completedCount = goalRepository.updateInProgressToCompleted(today)

        logger.info { "Goal status updated: $startedCount started, $completedCount completed" }
    }
}
