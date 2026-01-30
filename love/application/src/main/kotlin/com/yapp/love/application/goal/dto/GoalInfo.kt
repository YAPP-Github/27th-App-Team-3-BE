package com.yapp.love.application.goal.dto

import com.yapp.love.domain.goal.model.Goal
import com.yapp.love.domain.goal.model.GoalIcon
import com.yapp.love.domain.goal.model.GoalStatus
import com.yapp.love.domain.goal.model.RepeatCycle

data class GoalInfo(
    val goalId: Long,
    val name: String,
    val icon: GoalIcon,
    val goalStatus: GoalStatus,
    val repeatCycle: RepeatCycle,
    val repeatCount: Int,
    val startDate: String,
    val endDate: String?,
    val createdAt: String,
    val updatedAt: String,
) {
    companion object {
        fun from(goal: Goal): GoalInfo {
            return GoalInfo(
                goalId = goal.id!!,
                name = goal.name,
                icon = goal.icon,
                repeatCycle = goal.repeatCycle,
                repeatCount = goal.repeatCount,
                startDate = goal.startDate.toString(),
                endDate = goal.endDate?.toString(),
                goalStatus = goal.goalStatus,
                createdAt = goal.createdAt.toString(),
                updatedAt = goal.updatedAt.toString(),
            )
        }
    }
}
