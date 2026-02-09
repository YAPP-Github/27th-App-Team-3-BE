package com.yapp.love.application.goal

import com.yapp.love.application.couple.CoupleService
import com.yapp.love.application.goal.dto.CreateGoalCommand
import com.yapp.love.application.goal.dto.GoalInfo
import com.yapp.love.application.goal.dto.GoalWithPhotoLogs
import com.yapp.love.application.goal.dto.UpdateGoalCommand
import com.yapp.love.application.photolog.PhotologService
import com.yapp.love.domain.goal.model.Goal
import com.yapp.love.domain.goal.model.GoalStatus
import com.yapp.love.domain.goal.repository.GoalRepository
import com.yapp.love.globalutils.exception.GlobalErrorCode
import com.yapp.love.globalutils.exception.GlobalException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class GoalService(
    private val photologService: PhotologService,
    private val coupleService: CoupleService,
    private val goalRepository: GoalRepository,
) {
    private val logger = KotlinLogging.logger {}

    @Transactional
    fun createGoal(command: CreateGoalCommand): GoalInfo {
        val goal =
            try {
                Goal.of(
                    coupleId = command.coupleId,
                    name = command.name,
                    icon = command.icon,
                    repeatCycle = command.repeatCycle,
                    repeatCount = command.repeatCount,
                    startDate = command.startDate,
                    hasEndDate = command.endDate != null,
                    endDate = command.endDate,
                )
            } catch (e: IllegalArgumentException) {
                throw GlobalException(GlobalErrorCode.INVALID_INPUT_VALUE, "입력값이 올바르지 않습니다.")
            }

        val savedGoal = goalRepository.save(goal)
        return GoalInfo.from(savedGoal)
    }

    fun getGoalsByDate(
        coupleId: Long,
        targetDate: LocalDate,
    ): List<GoalInfo> {
        return goalRepository.findActiveGoalsByCoupleIdAndDate(coupleId, targetDate)
            .map { GoalInfo.from(it) }
    }

    fun getGoalsWithPhotologs(
        coupleId: Long,
        myUserId: Long,
        partnerUserId: Long,
        targetDate: LocalDate,
    ): List<GoalWithPhotoLogs> {
        val goals = goalRepository.findActiveGoalsByCoupleIdAndDate(coupleId, targetDate)

        if (goals.isEmpty()) {
            return emptyList()
        }

        val goalIds = goals.map { it.id!! }
        val photologs = photologService.findByGoalIdsAndVerificationDate(goalIds, targetDate)

        val photologsByGoalId = photologs.groupBy { it.goalId }

        return goals.map { goal ->
            val goalPhotologs = photologsByGoalId[goal.id] ?: emptyList()
            val myPhotolog = goalPhotologs.find { it.userId == myUserId }
            val partnerPhotolog = goalPhotologs.find { it.userId == partnerUserId }

            GoalWithPhotoLogs(
                goal = goal,
                myPhotolog = myPhotolog,
                partnerPhotolog = partnerPhotolog,
            )
        }
    }

    fun getGoalById(
        userId: Long,
        goalId: Long,
    ): GoalInfo {
        val goal = getGoalEntityById(goalId)
        verifyUserOwnsGoal(userId, goal)
        return GoalInfo.from(goal)
    }

    private fun getGoalEntityById(goalId: Long): Goal {
        return goalRepository.findActiveGoalById(goalId)
            ?: throw GlobalException(GlobalErrorCode.NOT_FOUND, "목표를 찾을 수 없습니다.")
    }

    private fun verifyUserOwnsGoal(
        userId: Long,
        goal: Goal,
    ) {
        val coupleInfo = coupleService.getCoupleInfoByUserId(userId)
        if (coupleInfo.id != goal.coupleId) {
            throw GlobalException(GlobalErrorCode.FORBIDDEN, "해당 목표에 대한 권한이 없습니다.")
        }
    }

    @Transactional
    fun updateGoal(
        userId: Long,
        goalId: Long,
        command: UpdateGoalCommand,
    ): GoalInfo {
        val goal = getGoalEntityById(goalId)
        verifyUserOwnsGoal(userId, goal)

        // 종료일이 변경되고, 새 종료일이 오늘 이전으로 설정되면 인증 기록 삭제
        if (goal.endDate != command.endDate && command.endDate != null) {
            val today = LocalDate.now()
            if (command.endDate.isBefore(today)) {
                photologService.deleteByGoalIdAfterEndDate(goalId, command.endDate)
                logger.info {
                    "Goal $goalId endDate changed to ${command.endDate}"
                }
            }
        }

        try {
            goal.name = command.name
            goal.icon = command.icon
            goal.updateRepeatSettings(command.repeatCycle, command.repeatCount)
            goal.updateEndDate(command.endDate != null, command.endDate)
        } catch (e: IllegalArgumentException) {
            throw GlobalException(GlobalErrorCode.INVALID_INPUT_VALUE, "입력값이 올바르지 않습니다.")
        }

        goalRepository.save(goal)
        return GoalInfo.from(goal)
    }

    @Transactional
    fun deleteGoal(
        userId: Long,
        goalId: Long,
    ): Boolean {
        val goal = getGoalEntityById(goalId)
        verifyUserOwnsGoal(userId, goal)

        goal.deletedAt = Instant.now()
        goal.goalStatus = GoalStatus.DELETED
        goalRepository.save(goal)

        // TODO: 인증 로직이 구현되면 인증도 soft delete하는 로직 추가
        return true
    }

    @Transactional
    fun completeGoal(
        userId: Long,
        goalId: Long,
    ): GoalInfo {
        val goal = getGoalEntityById(goalId)
        verifyUserOwnsGoal(userId, goal)

        // 진행 중인 목표만 완료할 수 있음
        if (goal.goalStatus != GoalStatus.IN_PROGRESS) {
            throw GlobalException(
                GlobalErrorCode.INVALID_INPUT_VALUE,
                "진행 중인 목표만 완료할 수 있습니다. (현재 상태: ${goal.goalStatus})",
            )
        }

        goal.goalStatus = GoalStatus.COMPLETED
        goalRepository.save(goal)

        logger.info { "Goal $goalId completed by user $userId" }
        // TODO: 통계가 구현되면 통계에 반영하는 로직 추가
        return GoalInfo.from(goal)
    }
}
