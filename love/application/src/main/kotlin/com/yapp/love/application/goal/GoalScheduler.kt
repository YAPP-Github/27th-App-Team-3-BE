package com.yapp.love.application.goal

import com.yapp.love.application.notification.event.GoalEndedEvent
import com.yapp.love.domain.couple.CoupleInfoRepository
import com.yapp.love.domain.goal.repository.GoalRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Component
class GoalScheduler(
    private val goalRepository: GoalRepository,
    private val coupleInfoRepository: CoupleInfoRepository,
    private val notificationEventPublisher: ApplicationEventPublisher,
) {
    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(name = "goalSchedulerProcessDaily", lockAtMostFor = "30m", lockAtLeastFor = "5m")
    @Transactional
    fun processDaily() {
        val today = LocalDate.now()
        logger.info { "목표 스케줄러 시작: $today" }

        // 1. 종료 대상 목표 알림 전송 (상태 업데이트 전에 조회)
        try {
            sendGoalEndedNotifications(today)
        } catch (e: Exception) {
            logger.error(e) { "목표 종료 알림 전송 실패" }
        }

        // 2. 시작일 도래 목표 활성화
        try {
            val started = goalRepository.updateNotStartedToInProgress(today)
            logger.info { "NOT_STARTED → IN_PROGRESS: ${started}건" }
        } catch (e: Exception) {
            logger.error(e) { "NOT_STARTED → IN_PROGRESS 처리 실패" }
        }

        // 3. 종료일 지난 목표 완료 처리
        try {
            val completed = goalRepository.updateInProgressToCompleted(today)
            logger.info { "IN_PROGRESS → COMPLETED: ${completed}건" }
        } catch (e: Exception) {
            logger.error(e) { "IN_PROGRESS → COMPLETED 처리 실패" }
        }
    }

    private fun sendGoalEndedNotifications(today: LocalDate) {
        val endingGoals = goalRepository.findGoalsEndingBefore(today)
        if (endingGoals.isEmpty()) return

        logger.info { "종료 대상 목표: ${endingGoals.size}건" }

        // coupleId별로 그룹핑하여 알림 전송
        val goalsByCoupleId = endingGoals.groupBy { it.coupleId }

        goalsByCoupleId.forEach { (coupleId, goals) ->
            val coupleInfo = coupleInfoRepository.findById(coupleId)
            if (coupleInfo == null) {
                logger.warn { "커플 정보를 찾을 수 없음: coupleId=$coupleId" }
                return@forEach
            }

            goals.forEach { goal ->
                notificationEventPublisher.publishEvent(
                    GoalEndedEvent(
                        user1Id = coupleInfo.user1Id,
                        user2Id = coupleInfo.user2Id,
                        goalId = goal.id!!,
                        goalName = goal.name,
                    )
                )
            }
        }
    }
}
