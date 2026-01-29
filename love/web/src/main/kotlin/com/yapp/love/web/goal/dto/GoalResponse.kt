package com.yapp.love.web.goal.dto

import com.yapp.love.domain.goal.model.Goal
import com.yapp.love.domain.goal.model.GoalIcon
import com.yapp.love.domain.goal.model.GoalStatus
import com.yapp.love.domain.goal.model.RepeatCycle

data class GoalResponse(
    val goalId: Long,
    val name: String,
    val icon: GoalIcon,
    val repeatCycle: RepeatCycle,
    val repeatCount: Int,
    val startDate: String,
    val endDate: String?,
    val status: GoalStatus,
    val createdAt: String
) {
    companion object {
        fun from(goalInfo: com.yapp.love.application.goal.dto.GoalInfo): GoalResponse {
            return GoalResponse(
                goalId = goalInfo.goalId,
                name = goalInfo.name,
                icon = goalInfo.icon,
                repeatCycle = goalInfo.repeatCycle,
                repeatCount = goalInfo.repeatCount,
                startDate = goalInfo.startDate,
                endDate = goalInfo.endDate,
                status = goalInfo.status,
                createdAt = goalInfo.createdAt
            )
        }
    }
}

data class GoalListResponse(
    val completedCount: Int,
    val totalCount: Int,
    val goals: List<GoalItemResponse>
)

data class GoalItemResponse(
    val goalId: Long,
    val name: String,
    val icon: GoalIcon,
    val repeatCycle: RepeatCycle,
    val myCompleted: Boolean,
    val partnerCompleted: Boolean,
    val myVerification: PhotologInfo?,
    val partnerVerification: PhotologInfo?
)

data class PhotologInfo(
    val photologId: Long,
    val imageUrl: String,
    val comment: String?,
    val reaction: String?,
    val uploadedAt: String
)

data class DeleteGoalResponse(
    val success: Boolean,
    val message: String
)

data class CompleteGoalResponse(
    val goalId: Long,
    val name: String,
    val status: GoalStatus,
    val completedAt: String
)
