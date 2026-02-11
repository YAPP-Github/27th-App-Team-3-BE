package com.yapp.love.application.goal.dto

import com.yapp.love.domain.goal.model.GoalIcon
import com.yapp.love.domain.goal.model.RepeatCycle
import java.time.LocalDate

data class CreateGoalCommand(
    val coupleId: Long,
    val goalName: String,
    val icon: GoalIcon,
    val repeatCycle: RepeatCycle,
    val repeatCount: Int,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
)

data class UpdateGoalCommand(
    val goalName: String,
    val icon: GoalIcon,
    val repeatCycle: RepeatCycle,
    val repeatCount: Int,
    val endDate: LocalDate? = null,
)
