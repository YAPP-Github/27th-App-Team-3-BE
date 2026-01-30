package com.yapp.love.domain.goal.model

import com.yapp.love.domain.common.BaseEntity
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(
    name = "goals",
    indexes = [
        Index(name = "idx_goals_couple_deleted_start", columnList = "couple_id, deleted_at, start_date"),
        // 스케줄러용 인덱스: IN_PROGRESS -> COMPLETED 전환
        Index(name = "idx_goals_status_enddate", columnList = "goal_status, has_end_date, end_date, deleted_at"),
        // 스케줄러용 인덱스: NOT_STARTED -> IN_PROGRESS 전환
        Index(name = "idx_goals_status_startdate", columnList = "goal_status, start_date, deleted_at"),
    ],
)
class Goal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "couple_id", nullable = false)
    val coupleId: Long,
    @Column(nullable = false)
    var name: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_cycle", nullable = false)
    var repeatCycle: RepeatCycle,
    @Column(name = "repeat_count", nullable = false)
    var repeatCount: Int,
    @Column(name = "start_date", nullable = false)
    val startDate: LocalDate,
    @Column(name = "has_end_date", nullable = false)
    var hasEndDate: Boolean = false,
    @Column(name = "end_date")
    var endDate: LocalDate? = null,
    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "goal_status", nullable = false, length = 50)
    var goalStatus: GoalStatus,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, name = "goal_icon")
    var icon: GoalIcon,
) : BaseEntity() {
    init {
        validateRepeatCycle()
        validateGoalDates()
    }

    fun updateRepeatSettings(
        newCycle: RepeatCycle,
        newCount: Int,
    ) {
        this.repeatCycle = newCycle
        this.repeatCount = newCount
        validateRepeatCycle()
    }

    fun updateEndDate(
        hasEndDate: Boolean,
        endDate: LocalDate?,
    ) {
        this.hasEndDate = hasEndDate
        this.endDate = endDate
        validateGoalDates()
    }

    private fun validateRepeatCycle() {
        when (repeatCycle) {
            RepeatCycle.DAILY -> {
                require(repeatCount == 1) {
                    "DAILY 목표는 하루 한번만 가능합니다."
                }
            }
            RepeatCycle.WEEKLY -> {
                require(repeatCount in 1..6) {
                    "WEEKLY 목표는 1번 이상 6번 이하로 가능합니다."
                }
            }
            RepeatCycle.MONTHLY -> {
                require(repeatCount in 1..25) {
                    "MONTHLY 목표는 1번 이상 25 이하로 가능합니다."
                }
            }
        }
    }

    /**
     * 생성 시점에만 적용되는 검증 (시간에 의존)
     * - 팩토리 메서드에서만 호출
     */
    private fun validateForCreation() {
        val today = LocalDate.now()
        require(!startDate.isBefore(today)) {
            "시작일은 오늘 또는 미래여야 합니다."
        }
    }

    /**
     * 항상 유효해야 하는 불변 조건 검증
     * - init 블록과 업데이트 메서드에서 호출
     */
    private fun validateGoalDates() {
        if (hasEndDate) {
            require(endDate != null) {
                "종료일이 설정된 목표는 종료일자를 입력해야 합니다."
            }
        }
        val date = endDate
        if (date != null) {
            require(!date.isBefore(startDate)) {
                "종료일은 시작일 이후여야 합니다."
            }
            require(!date.isEqual(startDate)) {
                "종료일은 시작일과 달라야 합니다."
            }
        }
    }

    companion object {
        fun of(
            coupleId: Long,
            name: String,
            icon: GoalIcon,
            repeatCycle: RepeatCycle,
            repeatCount: Int,
            startDate: LocalDate,
            hasEndDate: Boolean,
            endDate: LocalDate?,
        ): Goal {
            val today = LocalDate.now()
            val initialStatus =
                if (startDate.isAfter(today)) {
                    GoalStatus.NOT_STARTED
                } else {
                    GoalStatus.IN_PROGRESS
                }

            val goal =
                Goal(
                    coupleId = coupleId,
                    name = name,
                    icon = icon,
                    repeatCycle = repeatCycle,
                    repeatCount = repeatCount,
                    startDate = startDate,
                    hasEndDate = hasEndDate,
                    endDate = endDate,
                    goalStatus = initialStatus,
                    deletedAt = null,
                )

            // 생성 시점에만 필요한 검증 (시간 의존적)
            goal.validateForCreation()

            return goal
        }
    }
}
