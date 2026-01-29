package com.yapp.love.web.goal

import com.yapp.love.application.couple.CoupleService
import com.yapp.love.application.goal.GoalService
import com.yapp.love.application.goal.dto.CreateGoalCommand
import com.yapp.love.application.goal.dto.UpdateGoalCommand
import com.yapp.love.web.auth.AuthUser
import com.yapp.love.web.goal.dto.*
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Goal", description = "목표 API")
@RestController
@RequestMapping("/api/v1/goals")
class GoalController(
    private val goalService: GoalService,
    private val coupleService: CoupleService,
) {

    @CreateGoalApiSpec
    @PostMapping
    fun createGoal(
        @AuthUser userId: Long,
        @Valid @RequestBody request: CreateGoalRequest
    ): GoalResponse {
        val command = CreateGoalCommand(
            coupleId = request.coupleId,
            name = request.name,
            icon = request.icon,
            repeatCycle = request.repeatCycle,
            repeatCount = request.repeatCount,
            startDate = LocalDate.parse(request.startDate),
            endDate = request.endDate?.let { LocalDate.parse(it) }
        )
        val goalInfo = goalService.createGoal(command)
        return GoalResponse.from(goalInfo)
    }

    @GetGoalsApiSpec
    @GetMapping
    fun getGoals(
        @AuthUser userId: Long,
        @RequestParam date: String  // "2026-01-14"
    ): GoalListResponse {
        val coupleInfo = coupleService.getCoupleInfoByUserId(userId)
        val coupleId = coupleInfo.id!!
        val partnerUserId = coupleService.getPartnerUserIdByCoupleInfo(coupleInfo, userId)

        val targetDate = LocalDate.parse(date)
        val goalsWithPhotologs = goalService.getGoalsWithPhotologs(
            coupleId = coupleId,
            myUserId = userId,
            partnerUserId = partnerUserId,
            targetDate = targetDate
        )

        val goalItems = goalsWithPhotologs.map { goalWithPhotologs ->
            GoalItemResponse(
                goalId = goalWithPhotologs.goal.id!!,
                name = goalWithPhotologs.goal.name,
                icon = goalWithPhotologs.goal.icon,
                repeatCycle = goalWithPhotologs.goal.repeatCycle,
                myCompleted = goalWithPhotologs.myPhotolog != null,
                partnerCompleted = goalWithPhotologs.partnerPhotolog != null,
                myVerification = goalWithPhotologs.myPhotolog?.let {
                    PhotologInfo(
                        photologId = it.id!!,
                        imageUrl = it.imageUrl,
                        comment = it.comment,
                        reaction = it.reaction?.name,
                        uploadedAt = it.uploadedAt.toString()
                    )
                },
                partnerVerification = goalWithPhotologs.partnerPhotolog?.let {
                    PhotologInfo(
                        photologId = it.id!!,
                        imageUrl = it.imageUrl,
                        comment = it.comment,
                        reaction = it.reaction?.name,
                        uploadedAt = it.uploadedAt.toString()
                    )
                }
            )
        }

        // completedCount: 양쪽 모두 인증한 목표 개수
        val completedCount = goalsWithPhotologs.count {
            it.myPhotolog != null && it.partnerPhotolog != null
        }

        return GoalListResponse(
            completedCount = completedCount,
            totalCount = goalsWithPhotologs.size,
            goals = goalItems
        )
    }

    @GetGoalApiSpec
    @GetMapping("/{goalId}")
    fun getGoal(
        @AuthUser userId: Long,
        @PathVariable goalId: Long
    ): GoalResponse {
        val goal = goalService.getGoalById(goalId)
        return GoalResponse.from(goal)
    }

    @UpdateGoalApiSpec
    @PutMapping("/{goalId}")
    fun updateGoal(
        @AuthUser userId: Long,
        @PathVariable goalId: Long,
        @Valid @RequestBody request: UpdateGoalRequest
    ): GoalResponse {
        val command = UpdateGoalCommand(
            name = request.name,
            icon = request.icon,
            repeatCycle = request.repeatCycle,
            repeatCount = request.repeatCount,
            endDate = request.endDate?.let { LocalDate.parse(it) }
        )
        val goalInfo = goalService.updateGoal(goalId, command)
        return GoalResponse.from(goalInfo)
    }

    @DeleteGoalApiSpec
    @DeleteMapping("/{goalId}")
    fun deleteGoal(
        @AuthUser userId: Long,
        @PathVariable goalId: Long
    ): DeleteGoalResponse {
        goalService.deleteGoal(goalId)
        return DeleteGoalResponse(
            success = true,
            message = "목표가 삭제되었습니다."
        )
    }

    @CompleteGoalApiSpec
    @PatchMapping("/{goalId}/complete")
    fun completeGoal(
        @AuthUser userId: Long,
        @PathVariable goalId: Long
    ): CompleteGoalResponse {
        val goalInfo = goalService.completeGoal(goalId)
        return CompleteGoalResponse(
            goalId = goalInfo.goalId,
            name = goalInfo.name,
            status = goalInfo.status,
            completedAt = goalInfo.updatedAt
        )
    }

}
