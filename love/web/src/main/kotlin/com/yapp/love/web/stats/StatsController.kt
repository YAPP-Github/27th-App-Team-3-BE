package com.yapp.love.web.stats

import com.yapp.love.application.stats.StatsService
import com.yapp.love.application.storage.FileStoragePort
import com.yapp.love.domain.goal.model.GoalIcon
import com.yapp.love.domain.goal.model.GoalStatus
import com.yapp.love.domain.goal.model.RepeatCycle
import com.yapp.love.domain.stamp.model.StampColor
import com.yapp.love.domain.stamp.model.StampType
import com.yapp.love.web.auth.AuthUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
    private val fileStoragePort: FileStoragePort,
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
    @Operation(summary = "목표 상세 통계 - 캘린더 조회")
    @GetMapping("/{goalId}/calendar")
    fun getStatsCalendar(
        @AuthUser userId: Long,
        @PathVariable goalId: Long,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") selectedDate: YearMonth,
    ): StatsCalendar {
        val info = statsService.getStatsCalendar(userId, goalId, selectedDate)

        return StatsCalendar(
            goalId = info.goalId,
            goalName = info.goalName,
            goalIcon = info.goalIcon,
            yearMonth = info.yearMonth.toString(),
            isCompleted = info.isCompleted,
            completedDates = info.completedDates.map { completed ->
                CompletedDate(
                    date = completed.date.toString(),
                    myImageUrl = completed.myPhotolog?.let {
                        fileStoragePort.getPhotologUrl(info.goalId, it.fileName)
                    },
                    partnerImageUrl = completed.partnerPhotolog?.let {
                        fileStoragePort.getPhotologUrl(info.goalId, it.fileName)
                    },
                )
            },
        )
    }

    @Operation(summary = "목표 상세 통계 - 요약 조회")
    @GetMapping("/{goalId}/summary")
    fun getStatsSummary(
        @AuthUser userId: Long,
        @PathVariable goalId: Long,
    ): StatsSummary {
        val info = statsService.getStatsSummary(userId, goalId)

        return StatsSummary(
            myNickname = info.myNickname,
            partnerNickname = info.partnerNickname,
            totalCount = info.totalCount,
            myCompletedCount = info.myCompletedCount,
            partnerCompletedCount = info.partnerCompletedCount,
            repeatCycle = info.repeatCycle,
            startDate = info.startDate.toString(),
            endDate = info.endDate?.toString(),
        )
    }
}

data class StatsCalendar(
    val goalId: Long,
    val goalName: String,
    val goalIcon: GoalIcon,
    val yearMonth: String,
    val isCompleted: Boolean,
    val completedDates: List<CompletedDate>,
)

data class CompletedDate(
    val date: String,
    val myImageUrl: String?,
    val partnerImageUrl: String?,
)

data class StatsSummary(
    val myNickname: String,
    val partnerNickname: String,
    val totalCount: Int,
    val myCompletedCount: Int,
    val partnerCompletedCount: Int,
    val repeatCycle: RepeatCycle,
    val startDate: String,
    val endDate: String?,
)

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
