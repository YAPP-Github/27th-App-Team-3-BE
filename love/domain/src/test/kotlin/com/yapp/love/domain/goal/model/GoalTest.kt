package com.yapp.love.domain.goal.model

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GoalTest {
    @Nested
    @DisplayName("목표 생성 시 날짜 검증")
    inner class DateValidationTest {
        @Test
        @DisplayName("시작일은 과거일 수 없다")
        fun startDateCannotBePast() {
            val pastDate = LocalDate.now().minusDays(1)

            val exception =
                assertThrows<IllegalArgumentException> {
                    Goal.of(
                        coupleId = 1L,
                        name = "운동하기",
                        icon = GoalIcon.EXERCISE,
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        startDate = pastDate,
                        hasEndDate = false,
                        endDate = null,
                    )
                }
            assertTrue(exception.message!!.contains("시작일은 오늘 또는 미래여야 합니다"))
        }

        @Test
        @DisplayName("hasEndDate가 true면 endDate는 필수다")
        fun endDateIsRequiredWhenHasEndDateIsTrue() {
            val exception =
                assertThrows<IllegalArgumentException> {
                    Goal.of(
                        coupleId = 1L,
                        name = "운동하기",
                        icon = GoalIcon.EXERCISE,
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        startDate = LocalDate.now(),
                        hasEndDate = true,
                        endDate = null,
                    )
                }
            assertTrue(exception.message!!.contains("종료일이 설정된 목표는 종료일자를 입력해야 합니다"))
        }

        @Test
        @DisplayName("종료일이 시작일보다 이후면 생성 성공")
        fun goalWithValidDateRangeSucceeds() {
            val startDate = LocalDate.now()
            val endDate = LocalDate.now().plusDays(1)

            val goal =
                Goal.of(
                    coupleId = 1L,
                    name = "운동하기",
                    icon = GoalIcon.EXERCISE,
                    repeatCycle = RepeatCycle.DAILY,
                    repeatCount = 1,
                    startDate = startDate,
                    hasEndDate = true,
                    endDate = endDate,
                )

            assertEquals(startDate, goal.startDate)
            assertEquals(endDate, goal.endDate)
        }
    }

    @Nested
    @DisplayName("목표 생성 시 초기 상태 설정")
    inner class InitialStatusTest {
        @Test
        @DisplayName("시작일이 미래면 NOT_STARTED 상태로 생성된다")
        fun goalWithFutureStartDateHasNotStartedStatus() {
            val futureDate = LocalDate.now().plusDays(1)

            val goal =
                Goal.of(
                    coupleId = 1L,
                    name = "운동하기",
                    icon = GoalIcon.EXERCISE,
                    repeatCycle = RepeatCycle.DAILY,
                    repeatCount = 1,
                    startDate = futureDate,
                    hasEndDate = false,
                    endDate = null,
                )

            assertEquals(GoalStatus.NOT_STARTED, goal.goalStatus)
        }

        @Test
        @DisplayName("시작일이 오늘이면 IN_PROGRESS 상태로 생성된다")
        fun goalWithTodayStartDateHasInProgressStatus() {
            val today = LocalDate.now()

            val goal =
                Goal.of(
                    coupleId = 1L,
                    name = "운동하기",
                    icon = GoalIcon.EXERCISE,
                    repeatCycle = RepeatCycle.DAILY,
                    repeatCount = 1,
                    startDate = today,
                    hasEndDate = false,
                    endDate = null,
                )

            assertEquals(GoalStatus.IN_PROGRESS, goal.goalStatus)
        }
    }

    @Nested
    @DisplayName("목표 업데이트 시 검증")
    inner class UpdateValidationTest {
        @Test
        @DisplayName("updateEndDate로 시작일보다 이전 종료일 설정 시 예외 발생")
        fun updateEndDateWithDateBeforeStartDateFails() {
            val goal =
                Goal.of(
                    coupleId = 1L,
                    name = "운동하기",
                    icon = GoalIcon.EXERCISE,
                    repeatCycle = RepeatCycle.DAILY,
                    repeatCount = 1,
                    startDate = LocalDate.now(),
                    hasEndDate = false,
                    endDate = null,
                )

            val exception =
                assertThrows<IllegalArgumentException> {
                    goal.updateEndDate(true, LocalDate.now().minusDays(1))
                }
            assertTrue(exception.message!!.contains("종료일은 시작일 이후여야 합니다"))
        }

        @Test
        @DisplayName("updateEndDate로 시작일과 같은 종료일 설정 시 예외 발생")
        fun updateEndDateWithDateEqualToStartDateFails() {
            val startDate = LocalDate.now()
            val goal =
                Goal.of(
                    coupleId = 1L,
                    name = "운동하기",
                    icon = GoalIcon.EXERCISE,
                    repeatCycle = RepeatCycle.DAILY,
                    repeatCount = 1,
                    startDate = startDate,
                    hasEndDate = false,
                    endDate = null,
                )

            val exception =
                assertThrows<IllegalArgumentException> {
                    goal.updateEndDate(true, startDate)
                }
            assertTrue(exception.message!!.contains("종료일은 시작일과 달라야 합니다"))
        }

        @Test
        @DisplayName("updateEndDate로 종료일 제거 시 성공")
        fun updateEndDateToRemoveEndDateSucceeds() {
            val goal =
                Goal.of(
                    coupleId = 1L,
                    name = "운동하기",
                    icon = GoalIcon.EXERCISE,
                    repeatCycle = RepeatCycle.DAILY,
                    repeatCount = 1,
                    startDate = LocalDate.now(),
                    hasEndDate = true,
                    endDate = LocalDate.now().plusDays(30),
                )

            goal.updateEndDate(false, null)

            assertFalse(goal.hasEndDate)
            assertNull(goal.endDate)
        }
    }
}
