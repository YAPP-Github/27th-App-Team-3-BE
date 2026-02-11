package com.yapp.love.web.goal.dto

import com.yapp.love.domain.goal.model.GoalIcon
import com.yapp.love.domain.goal.model.RepeatCycle
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class CreateGoalRequest(
    @field:NotBlank(message = "목표 이름은 필수입니다")
    val goalName: String,
    @field:NotNull(message = "아이콘은 필수입니다")
    val icon: GoalIcon,
    @field:NotNull(message = "반복 주기는 필수입니다")
    val repeatCycle: RepeatCycle,
    @field:NotNull(message = "반복 횟수는 필수입니다")
    @field:Min(value = 1, message = "반복 횟수는 1 이상이어야 합니다")
    val repeatCount: Int,
    @field:NotNull(message = "시작일은 필수입니다")
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
)

data class UpdateGoalRequest(
    @field:NotBlank(message = "목표 이름은 필수입니다")
    val goalName: String,
    @field:NotNull(message = "아이콘은 필수입니다")
    val icon: GoalIcon,
    @field:NotNull(message = "반복 주기는 필수입니다")
    val repeatCycle: RepeatCycle,
    @field:NotNull(message = "반복 횟수는 필수입니다")
    @field:Min(value = 1, message = "반복 횟수는 1 이상이어야 합니다")
    val repeatCount: Int,
    val endDate: LocalDate? = null,
)
