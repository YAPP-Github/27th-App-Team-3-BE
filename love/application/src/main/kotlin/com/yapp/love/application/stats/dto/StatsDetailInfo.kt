package com.yapp.love.application.stats.dto

import com.yapp.love.domain.goal.model.GoalIcon
import com.yapp.love.domain.goal.model.GoalStatus
import com.yapp.love.domain.goal.model.RepeatCycle
import com.yapp.love.domain.photolog.model.Photolog
import java.time.LocalDate
import java.time.YearMonth

data class StatsCalendarInfo(
    val goalId: Long,
    val goalName: String,
    val goalIcon: GoalIcon,
    val yearMonth: YearMonth,
    val isCompleted: Boolean,
    val completedDates: List<CompletedDateInfo>,
)

data class CompletedDateInfo(
    val date: LocalDate,
    val myPhotolog: Photolog?,
    val partnerPhotolog: Photolog?,
)

data class StatsSummaryInfo(
    val myNickname: String,
    val partnerNickname: String,
    val totalCount: Int,
    val myCompletedCount: Int,
    val partnerCompletedCount: Int,
    val repeatCycle: RepeatCycle,
    val startDate: LocalDate,
    val endDate: LocalDate?,
)
