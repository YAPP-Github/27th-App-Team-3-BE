package com.yapp.love.application.stats.dto

import com.yapp.love.domain.goal.model.GoalIcon
import com.yapp.love.domain.stamp.model.StampColor
import com.yapp.love.domain.stamp.model.StampType
import java.time.LocalDate

data class StatsInfo(
    val selectedDate: LocalDate,
    val statsGoals: List<StatsGoalInfo>,
)

data class StatsGoalInfo(
    val goalId: Long,
    val goalName: String,
    val goalIconType: GoalIcon,
    val monthlyTargetCount: Int,
    val stamp: StampType,
    val myStats: ParticipantStatsInfo,
    val partnerStats: ParticipantStatsInfo,
)

data class ParticipantStatsInfo(
    val nickname: String,
    val endCount: Int,
    val stampColors: List<StampColor>,
)
