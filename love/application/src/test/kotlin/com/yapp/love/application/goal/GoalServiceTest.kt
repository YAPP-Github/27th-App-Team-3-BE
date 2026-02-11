package com.yapp.love.application.goal

import com.yapp.love.application.couple.CoupleService
import com.yapp.love.application.goal.dto.UpdateGoalCommand
import com.yapp.love.application.photolog.PhotologService
import com.yapp.love.domain.couple.model.CoupleInfo
import com.yapp.love.domain.goal.model.Goal
import com.yapp.love.domain.goal.model.GoalIcon
import com.yapp.love.domain.goal.model.GoalStatus
import com.yapp.love.domain.goal.model.RepeatCycle
import com.yapp.love.domain.goal.repository.GoalRepository
import com.yapp.love.globalutils.exception.GlobalException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.time.LocalDate

class GoalServiceTest : DescribeSpec({

    val goalRepository = mockk<GoalRepository>()
    val photologService = mockk<PhotologService>()
    val coupleService = mockk<CoupleService>()

    val goalService =
        GoalService(
            goalRepository = goalRepository,
            photologService = photologService,
            coupleService = coupleService,
        )

    beforeEach {
        clearAllMocks()
    }

    describe("getGoalsByDate") {

        val coupleId = 100L
        val targetDate = LocalDate.of(2026, 2, 9)

        context("해당 날짜에 목표가 존재하는 경우") {

            it("목표 목록을 GoalInfo로 변환하여 반환해야 함") {
                // given
                val goals =
                    listOf(
                        Goal(
                            id = 1L,
                            coupleId = coupleId,
                            name = "운동하기",
                            repeatCycle = RepeatCycle.DAILY,
                            repeatCount = 1,
                            startDate = LocalDate.of(2026, 2, 1),
                            hasEndDate = true,
                            endDate = LocalDate.of(2026, 3, 1),
                            goalStatus = GoalStatus.IN_PROGRESS,
                            icon = GoalIcon.ICON_EXERCISE,
                        ),
                        Goal(
                            id = 2L,
                            coupleId = coupleId,
                            name = "독서",
                            repeatCycle = RepeatCycle.WEEKLY,
                            repeatCount = 3,
                            startDate = LocalDate.of(2026, 2, 1),
                            hasEndDate = false,
                            endDate = null,
                            goalStatus = GoalStatus.IN_PROGRESS,
                            icon = GoalIcon.ICON_BOOK,
                        ),
                    )

                every {
                    goalRepository.findActiveGoalsByCoupleIdAndDate(coupleId, targetDate)
                } returns goals

                // when
                val result = goalService.getGoalsByDate(coupleId, targetDate)

                // then
                result.size shouldBe 2
                result[0].goalId shouldBe 1L
                result[0].goalName shouldBe "운동하기"
                result[0].repeatCycle shouldBe RepeatCycle.DAILY
                result[0].icon shouldBe GoalIcon.ICON_EXERCISE
                result[0].endDate shouldBe "2026-03-01"

                result[1].goalId shouldBe 2L
                result[1].goalName shouldBe "독서"
                result[1].repeatCycle shouldBe RepeatCycle.WEEKLY
                result[1].icon shouldBe GoalIcon.ICON_BOOK
                result[1].endDate shouldBe null
            }
        }

        context("해당 날짜에 목표가 없는 경우") {

            it("빈 리스트를 반환해야 함") {
                // given
                every {
                    goalRepository.findActiveGoalsByCoupleIdAndDate(coupleId, targetDate)
                } returns emptyList()

                // when
                val result = goalService.getGoalsByDate(coupleId, targetDate)

                // then
                result shouldBe emptyList()
            }
        }
    }

    describe("updateGoal - 포토로그 삭제 로직") {

        val userId = 1L
        val coupleId = 100L
        val goalId = 200L
        val partnerId = 2L

        val coupleInfo =
            CoupleInfo(
                id = coupleId,
                user1Id = userId,
                user2Id = partnerId,
                inviteCodeId = 1L,
                anniversaryDate = LocalDate.of(2024, 1, 1),
            )

        context("종료일이 과거로 변경되는 경우") {

            it("변경된 종료일 이후의 포토로그를 삭제해야 함") {
                // given
                val originalEndDate = LocalDate.of(2026, 2, 15)
                val newEndDate = LocalDate.of(2026, 1, 25)

                val goal =
                    Goal(
                        id = goalId,
                        coupleId = coupleId,
                        name = "운동하기",
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        startDate = LocalDate.of(2026, 1, 1),
                        hasEndDate = true,
                        endDate = originalEndDate,
                        goalStatus = GoalStatus.IN_PROGRESS,
                        icon = GoalIcon.ICON_DEFAULT,
                    )

                val command =
                    UpdateGoalCommand(
                        goalName = "운동하기",
                        icon = GoalIcon.ICON_DEFAULT,
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        endDate = newEndDate,
                    )

                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every { coupleService.getCoupleInfoByUserId(userId) } returns coupleInfo
                every { photologService.deleteByGoalIdAfterEndDate(goalId, newEndDate) } just Runs
                every { goalRepository.save(goal) } returns goal

                // when
                goalService.updateGoal(userId, goalId, command)

                // then
                verify(exactly = 1) {
                    photologService.deleteByGoalIdAfterEndDate(goalId, newEndDate)
                }
            }

            it("종료일을 오늘 이전으로 변경하면 해당 날짜 이후 포토로그가 삭제됨") {
                // given
                val today = LocalDate.now()
                val newEndDate = today.minusDays(3) // 3일 전

                val goal =
                    Goal(
                        id = goalId,
                        coupleId = coupleId,
                        name = "운동하기",
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        startDate = today.minusMonths(1),
                        hasEndDate = true,
                        endDate = today.plusDays(10), // 원래 종료일은 미래
                        goalStatus = GoalStatus.IN_PROGRESS,
                        icon = GoalIcon.ICON_DEFAULT,
                    )

                val command =
                    UpdateGoalCommand(
                        goalName = "운동하기",
                        icon = GoalIcon.ICON_DEFAULT,
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        endDate = newEndDate,
                    )

                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every { coupleService.getCoupleInfoByUserId(userId) } returns coupleInfo
                every { photologService.deleteByGoalIdAfterEndDate(goalId, newEndDate) } just Runs
                every { goalRepository.save(goal) } returns goal

                // when
                goalService.updateGoal(userId, goalId, command)

                // then
                verify(exactly = 1) {
                    photologService.deleteByGoalIdAfterEndDate(goalId, newEndDate)
                }
            }
        }

        context("종료일이 미래로 변경되는 경우") {

            it("포토로그를 삭제하지 않아야 함") {
                // given
                val originalEndDate = LocalDate.of(2026, 1, 25)
                val newEndDate = LocalDate.of(2026, 2, 15) // 미래로 변경

                val goal =
                    Goal(
                        id = goalId,
                        coupleId = coupleId,
                        name = "운동하기",
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        startDate = LocalDate.of(2026, 1, 1),
                        hasEndDate = true,
                        endDate = originalEndDate,
                        goalStatus = GoalStatus.IN_PROGRESS,
                        icon = GoalIcon.ICON_DEFAULT,
                    )

                val command =
                    UpdateGoalCommand(
                        goalName = "운동하기",
                        icon = GoalIcon.ICON_DEFAULT,
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        endDate = newEndDate,
                    )

                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every { coupleService.getCoupleInfoByUserId(userId) } returns coupleInfo
                every { goalRepository.save(goal) } returns goal

                // when
                goalService.updateGoal(userId, goalId, command)

                // then
                verify(exactly = 0) {
                    photologService.deleteByGoalIdAfterEndDate(any(), any())
                }
            }

            it("종료일을 오늘로 변경하면 포토로그를 삭제하지 않아야 함") {
                // given
                val today = LocalDate.now()
                val newEndDate = today

                val goal =
                    Goal(
                        id = goalId,
                        coupleId = coupleId,
                        name = "운동하기",
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        startDate = today.minusMonths(1),
                        hasEndDate = true,
                        endDate = today.minusDays(10),
                        goalStatus = GoalStatus.IN_PROGRESS,
                        icon = GoalIcon.ICON_DEFAULT,
                    )

                val command =
                    UpdateGoalCommand(
                        goalName = "운동하기",
                        icon = GoalIcon.ICON_DEFAULT,
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        endDate = newEndDate,
                    )

                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every { coupleService.getCoupleInfoByUserId(userId) } returns coupleInfo
                every { goalRepository.save(goal) } returns goal

                // when
                goalService.updateGoal(userId, goalId, command)

                // then
                verify(exactly = 0) {
                    photologService.deleteByGoalIdAfterEndDate(any(), any())
                }
            }
        }

        context("종료일이 변경되지 않는 경우") {

            it("종료일을 동일한 값으로 변경하면 포토로그를 삭제하지 않아야 함") {
                // given
                val endDate = LocalDate.of(2026, 1, 25)

                val goal =
                    Goal(
                        id = goalId,
                        coupleId = coupleId,
                        name = "운동하기",
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        startDate = LocalDate.of(2026, 1, 1),
                        hasEndDate = true,
                        endDate = endDate,
                        goalStatus = GoalStatus.IN_PROGRESS,
                        icon = GoalIcon.ICON_DEFAULT,
                    )

                val command =
                    UpdateGoalCommand(
                        goalName = "운동하기 (수정)", // 이름만 변경
                        icon = GoalIcon.ICON_DEFAULT,
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        endDate = endDate, // 동일한 종료일
                    )

                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every { coupleService.getCoupleInfoByUserId(userId) } returns coupleInfo
                every { goalRepository.save(goal) } returns goal

                // when
                goalService.updateGoal(userId, goalId, command)

                // then
                verify(exactly = 0) {
                    photologService.deleteByGoalIdAfterEndDate(any(), any())
                }
            }
        }

        context("종료일을 새로 추가하는 경우") {

            it("종료일 없던 목표에 과거 종료일을 추가하면 포토로그를 삭제해야 함") {
                // given
                val newEndDate = LocalDate.now().minusDays(5)

                val goal =
                    Goal(
                        id = goalId,
                        coupleId = coupleId,
                        name = "운동하기",
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        startDate = LocalDate.of(2026, 1, 1),
                        hasEndDate = false,
                        endDate = null,
                        goalStatus = GoalStatus.IN_PROGRESS,
                        icon = GoalIcon.ICON_DEFAULT,
                    )

                val command =
                    UpdateGoalCommand(
                        goalName = "운동하기",
                        icon = GoalIcon.ICON_DEFAULT,
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        endDate = newEndDate, // 과거 날짜로 종료일 추가
                    )

                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every { coupleService.getCoupleInfoByUserId(userId) } returns coupleInfo
                every { photologService.deleteByGoalIdAfterEndDate(goalId, newEndDate) } just Runs
                every { goalRepository.save(goal) } returns goal

                // when
                goalService.updateGoal(userId, goalId, command)

                // then
                verify(exactly = 1) {
                    photologService.deleteByGoalIdAfterEndDate(goalId, newEndDate)
                }
            }
        }

        context("포토로그 삭제 중 예외 발생") {

            it("포토로그 삭제 실패 시 예외를 전파해야 함") {
                // given
                val newEndDate = LocalDate.now().minusDays(3)

                val goal =
                    Goal(
                        id = goalId,
                        coupleId = coupleId,
                        name = "운동하기",
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        startDate = LocalDate.of(2026, 1, 1),
                        hasEndDate = true,
                        endDate = LocalDate.now().plusDays(10),
                        goalStatus = GoalStatus.IN_PROGRESS,
                        icon = GoalIcon.ICON_DEFAULT,
                    )

                val command =
                    UpdateGoalCommand(
                        goalName = "운동하기",
                        icon = GoalIcon.ICON_DEFAULT,
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        endDate = newEndDate,
                    )

                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every { coupleService.getCoupleInfoByUserId(userId) } returns coupleInfo
                every {
                    photologService.deleteByGoalIdAfterEndDate(goalId, newEndDate)
                } throws
                    GlobalException(
                        com.yapp.love.globalutils.exception.GlobalErrorCode.INTERNAL_SERVER_ERROR,
                        "인증 기록 삭제 중 오류가 발생했습니다.",
                    )

                // when & then
                shouldThrow<GlobalException> {
                    goalService.updateGoal(userId, goalId, command)
                }

                // 포토로그 삭제 실패 시 goal은 저장되지 않아야 함
                verify(exactly = 0) {
                    goalRepository.save(any())
                }
            }
        }

        context("종료된 목표 수정 방지") {

            it("COMPLETED 목표를 수정하면 예외가 발생해야 함") {
                // given
                val goal =
                    Goal(
                        id = goalId,
                        coupleId = coupleId,
                        name = "운동하기",
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        startDate = LocalDate.of(2026, 1, 1),
                        hasEndDate = true,
                        endDate = LocalDate.now(),
                        goalStatus = GoalStatus.COMPLETED,
                        icon = GoalIcon.ICON_DEFAULT,
                    )

                val command =
                    UpdateGoalCommand(
                        goalName = "운동하기 수정",
                        icon = GoalIcon.ICON_DEFAULT,
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        endDate = LocalDate.now().plusMonths(1),
                    )

                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every { coupleService.getCoupleInfoByUserId(userId) } returns coupleInfo

                // when & then
                val exception =
                    shouldThrow<GlobalException> {
                        goalService.updateGoal(userId, goalId, command)
                    }

                exception.getCustomMessage() shouldBe "종료된 목표는 수정할 수 없습니다."
                verify(exactly = 0) { goalRepository.save(any()) }
            }
        }

        context("권한 검증 - updateGoal") {

            it("다른 커플의 목표 수정 시 예외가 발생해야 함") {
                // given
                val otherCoupleId = 999L
                val otherCoupleInfo =
                    CoupleInfo(
                        id = otherCoupleId,
                        user1Id = userId,
                        user2Id = partnerId,
                        inviteCodeId = 2L,
                        anniversaryDate = LocalDate.of(2024, 1, 1),
                    )

                val goal =
                    Goal(
                        id = goalId,
                        coupleId = coupleId, // 다른 커플의 목표
                        name = "운동하기",
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        startDate = LocalDate.of(2026, 1, 1),
                        hasEndDate = true,
                        endDate = LocalDate.of(2026, 2, 15),
                        goalStatus = GoalStatus.IN_PROGRESS,
                        icon = GoalIcon.ICON_DEFAULT,
                    )

                val command =
                    UpdateGoalCommand(
                        goalName = "운동하기",
                        icon = GoalIcon.ICON_DEFAULT,
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        endDate = LocalDate.now().minusDays(1),
                    )

                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every { coupleService.getCoupleInfoByUserId(userId) } returns otherCoupleInfo

                // when & then
                val exception =
                    shouldThrow<GlobalException> {
                        goalService.updateGoal(userId, goalId, command)
                    }

                exception.getCustomMessage() shouldBe "해당 목표에 대한 권한이 없습니다."

                // 권한 없으면 포토로그 삭제도 시도하지 않아야 함
                verify(exactly = 0) {
                    photologService.deleteByGoalIdAfterEndDate(any(), any())
                }
            }
        }
    }

    describe("completeGoal") {

        val userId = 1L
        val coupleId = 100L
        val goalId = 200L
        val partnerId = 2L

        val coupleInfo =
            CoupleInfo(
                id = coupleId,
                user1Id = userId,
                user2Id = partnerId,
                inviteCodeId = 1L,
                anniversaryDate = LocalDate.of(2024, 1, 1),
            )

        context("진행 중인 목표를 종료하는 경우") {

            it("goalStatus가 COMPLETED로 변경되고 endDate가 오늘로 설정되어야 함") {
                // given
                val today = LocalDate.now()
                val goal =
                    Goal(
                        id = goalId,
                        coupleId = coupleId,
                        name = "운동하기",
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        startDate = today.minusMonths(1),
                        hasEndDate = true,
                        endDate = today.plusMonths(1),
                        goalStatus = GoalStatus.IN_PROGRESS,
                        icon = GoalIcon.ICON_EXERCISE,
                    )

                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every { coupleService.getCoupleInfoByUserId(userId) } returns coupleInfo
                every { goalRepository.save(goal) } returns goal

                // when
                val result = goalService.completeGoal(userId, goalId)

                // then
                goal.goalStatus shouldBe GoalStatus.COMPLETED
                goal.hasEndDate shouldBe true
                goal.endDate shouldBe today
                result.goalId shouldBe goalId
            }

            it("종료일이 없는 목표를 종료하면 hasEndDate가 true로 바뀌어야 함") {
                // given
                val today = LocalDate.now()
                val goal =
                    Goal(
                        id = goalId,
                        coupleId = coupleId,
                        name = "독서",
                        repeatCycle = RepeatCycle.WEEKLY,
                        repeatCount = 3,
                        startDate = today.minusMonths(1),
                        hasEndDate = false,
                        endDate = null,
                        goalStatus = GoalStatus.IN_PROGRESS,
                        icon = GoalIcon.ICON_BOOK,
                    )

                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every { coupleService.getCoupleInfoByUserId(userId) } returns coupleInfo
                every { goalRepository.save(goal) } returns goal

                // when
                goalService.completeGoal(userId, goalId)

                // then
                goal.goalStatus shouldBe GoalStatus.COMPLETED
                goal.hasEndDate shouldBe true
                goal.endDate shouldBe today
            }
        }

        context("진행 중이 아닌 목표를 종료하는 경우") {

            it("이미 COMPLETED인 목표를 종료하면 예외가 발생해야 함") {
                // given
                val goal =
                    Goal(
                        id = goalId,
                        coupleId = coupleId,
                        name = "운동하기",
                        repeatCycle = RepeatCycle.DAILY,
                        repeatCount = 1,
                        startDate = LocalDate.now().minusMonths(1),
                        hasEndDate = true,
                        endDate = LocalDate.now(),
                        goalStatus = GoalStatus.COMPLETED,
                        icon = GoalIcon.ICON_EXERCISE,
                    )

                every { goalRepository.findActiveGoalById(goalId) } returns goal
                every { coupleService.getCoupleInfoByUserId(userId) } returns coupleInfo

                // when & then
                val exception =
                    shouldThrow<GlobalException> {
                        goalService.completeGoal(userId, goalId)
                    }

                exception.getCustomMessage() shouldBe "진행 중인 목표만 완료할 수 있습니다. (현재 상태: COMPLETED)"
                verify(exactly = 0) { goalRepository.save(any()) }
            }
        }

        context("존재하지 않는 목표를 종료하는 경우") {

            it("NOT_FOUND 예외가 발생해야 함") {
                // given
                every { goalRepository.findActiveGoalById(goalId) } returns null

                // when & then
                shouldThrow<GlobalException> {
                    goalService.completeGoal(userId, goalId)
                }
            }
        }
    }
})
