package com.yapp.love.application.stats

import com.yapp.love.application.stats.dto.ParticipantStatsInfo
import com.yapp.love.application.stats.dto.StatsGoalInfo
import com.yapp.love.application.stats.dto.StatsInfo
import com.yapp.love.domain.couple.CoupleInfoRepository
import com.yapp.love.domain.goal.model.Goal
import com.yapp.love.domain.goal.model.GoalStatus
import com.yapp.love.domain.goal.model.RepeatCycle
import com.yapp.love.domain.goal.repository.GoalRepository
import com.yapp.love.domain.stamp.repository.StampHistoryRepository
import com.yapp.love.domain.user.UserAdditionInfoRepository
import com.yapp.love.globalutils.exception.GlobalErrorCode
import com.yapp.love.globalutils.exception.GlobalException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@Service
@Transactional(readOnly = true)
class StatsService(
    private val coupleInfoRepository: CoupleInfoRepository,
    private val goalRepository: GoalRepository,
    private val stampHistoryRepository: StampHistoryRepository,
    private val userAdditionInfoRepository: UserAdditionInfoRepository,
) {
    fun getMonthlyStats(
        userId: Long,
        yearMonth: YearMonth,
        goalStatus: GoalStatus,
    ): StatsInfo {
        val coupleInfo = coupleInfoRepository.findByUserId(userId)
            ?: throw GlobalException(GlobalErrorCode.FORBIDDEN, "커플 정보가 없습니다.")

        val myUserId = userId
        val partnerUserId = if (coupleInfo.user1Id == userId) coupleInfo.user2Id else coupleInfo.user1Id

        // 해당 월의 시작일과 종료일
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()

        // 커플의 목표 조회 (상태별 + 해당 월 기간 겹침 필터링)
        val goals = goalRepository.findActiveByCoupleId(coupleInfo.id!!)
            .filter { it.goalStatus == goalStatus }
            .filter { goal ->
                val actualStartDate = maxOf(startDate, goal.startDate)
                val goalEndDate = goal.endDate
                val actualEndDate = if (goal.hasEndDate && goalEndDate != null) {
                    minOf(endDate, goalEndDate)
                } else {
                    endDate
                }
                !actualStartDate.isAfter(actualEndDate)
            }

        if (goals.isEmpty()) {
            return StatsInfo(
                selectedDate = startDate,
                statsGoals = emptyList(),
            )
        }

        val goalIds = goals.mapNotNull { it.id }

        // 해당 월의 모든 스탬프 히스토리 조회
        val stampHistories = stampHistoryRepository.findByGoalIdsAndUserIdsAndStampDateBetween(
            goalIds = goalIds,
            userIds = listOf(myUserId, partnerUserId),
            startDate = startDate,
            endDate = endDate,
        )

        // goalId와 userId별로 스탬프 히스토리 그룹핑
        val stampHistoryMap = stampHistories.groupBy { it.goalId to it.userId }

        // 닉네임 조회
        val myNickname = userAdditionInfoRepository.findByUserId(myUserId)?.nickname ?: "나"
        val partnerNickname = userAdditionInfoRepository.findByUserId(partnerUserId)?.nickname ?: "상대방"

        val statsGoals = goals.map { goal ->
            val goalId = goal.id!!
            val myStampHistories = stampHistoryMap[goalId to myUserId] ?: emptyList()
            val partnerStampHistories = stampHistoryMap[goalId to partnerUserId] ?: emptyList()

            StatsGoalInfo(
                goalId = goalId,
                goalName = goal.name,
                goalIconType = goal.icon,
                monthlyTargetCount = calculateMonthlyTargetCount(goal, yearMonth),
                stamp = goal.stampType,
                myStats = ParticipantStatsInfo(
                    nickname = myNickname,
                    endCount = myStampHistories.size,
                    stampColors = myStampHistories.map { it.stampColor },
                ),
                partnerStats = ParticipantStatsInfo(
                    nickname = partnerNickname,
                    endCount = partnerStampHistories.size,
                    stampColors = partnerStampHistories.map { it.stampColor },
                ),
            )
        }

        return StatsInfo(
            selectedDate = startDate,
            statsGoals = statsGoals,
        )
    }

    private fun calculateMonthlyTargetCount(
        goal: Goal,
        yearMonth: YearMonth,
    ): Int {
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()

        // 목표의 실제 시작일과 종료일 고려
        val actualStartDate = maxOf(startDate, goal.startDate)
        val goalEndDate = goal.endDate
        val actualEndDate = if (goal.hasEndDate && goalEndDate != null) {
            minOf(endDate, goalEndDate)
        } else {
            endDate
        }

        if (actualStartDate.isAfter(actualEndDate)) {
            return 0
        }

        return when (goal.repeatCycle) {
            RepeatCycle.DAILY -> {
                // 해당 월의 일수
                ChronoUnit.DAYS.between(actualStartDate, actualEndDate).toInt() + 1
            }
            RepeatCycle.WEEKLY -> {
                // 해당 월의 주 수 * 주당 반복 횟수
                val weeks = calculateWeeksInMonth(actualStartDate, actualEndDate)
                weeks * goal.repeatCount
            }
            RepeatCycle.MONTHLY -> {
                // 월 목표이므로 repeatCount가 그대로 목표 횟수
                goal.repeatCount
            }
        }
    }

    private fun calculateWeeksInMonth(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Int {
        val days = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        return (days + 6) / 7 // 올림 처리
    }
}
