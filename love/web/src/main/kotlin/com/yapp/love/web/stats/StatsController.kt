package com.yapp.love.web.stats

import com.yapp.love.application.stats.StatsService
import com.yapp.love.domain.goal.model.GoalIcon
import com.yapp.love.domain.goal.model.GoalStatus
import com.yapp.love.domain.stamp.model.StampColor
import com.yapp.love.domain.stamp.model.StampType
import com.yapp.love.web.auth.AuthUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.YearMonth

@Tag(name = "Stats", description = "통계 API")
@RestController
@RequestMapping("/api/v1/stats")
class StatsController(
    private val statsService: StatsService,
) {
    @Operation(summary = "월별 스탬프 통계 조회")
    @GetMapping
    fun getMonthlyStats(
        @AuthUser userId: Long,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") selectedDate: YearMonth,
        @RequestParam(required = false, defaultValue = "IN_PROGRESS") status: GoalStatus,
    ): Stats {
        val statsInfo = statsService.getMonthlyStats(userId, selectedDate, status)

        return Stats(
            selectedDate = statsInfo.selectedDate,
            statsGoals = statsInfo.statsGoals.map { statsGoal ->
                StatsGoal(
                    goalId = statsGoal.goalId,
                    goalName = statsGoal.goalName,
                    goalIconType = statsGoal.goalIconType,
                    monthlyTargetCount = statsGoal.monthlyTargetCount,
                    stamp = statsGoal.stamp,
                    myStats = ParticipantStats(
                        nickname = statsGoal.myStats.nickname,
                        endCount = statsGoal.myStats.endCount,
                        stampColors = statsGoal.myStats.stampColors,
                    ),
                    partnerStats = ParticipantStats(
                        nickname = statsGoal.partnerStats.nickname,
                        endCount = statsGoal.partnerStats.endCount,
                        stampColors = statsGoal.partnerStats.stampColors,
                    ),
                )
            },
        )
    }
}

data class Stats(
    val selectedDate: LocalDate,
    val statsGoals: List<StatsGoal>,
)

data class StatsGoal(
    val goalId: Long,
    val goalName: String,
    val goalIconType: GoalIcon,
    val monthlyTargetCount: Int,
    val stamp: StampType,
    val myStats: ParticipantStats,
    val partnerStats: ParticipantStats,
)

data class ParticipantStats(
    val nickname: String,
    val endCount: Int,
    val stampColors: List<StampColor>,
)
