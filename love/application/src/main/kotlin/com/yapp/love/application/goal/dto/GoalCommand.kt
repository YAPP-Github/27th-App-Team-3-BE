package com.yapp.love.application.goal.dto

import com.yapp.love.domain.goal.model.GoalIcon
import com.yapp.love.domain.goal.model.RepeatCycle
import java.time.LocalDate

data class CreateGoalCommand(
    val coupleId: Long,
    val name: String,
    val icon: GoalIcon,
    val repeatCycle: RepeatCycle,
    val repeatCount: Int,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
)

data class UpdateGoalCommand(
    val name: String,
    val icon: GoalIcon,
    val repeatCycle: RepeatCycle,
    val repeatCount: Int,
    val endDate: LocalDate? = null,
)
