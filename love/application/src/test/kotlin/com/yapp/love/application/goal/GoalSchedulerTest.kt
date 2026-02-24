package com.yapp.love.application.goal

import com.yapp.love.application.notification.event.GoalEndedEvent
import com.yapp.love.domain.couple.CoupleInfoRepository
import com.yapp.love.domain.couple.model.CoupleInfo
import com.yapp.love.domain.goal.model.Goal
import com.yapp.love.domain.goal.model.GoalIcon
import com.yapp.love.domain.goal.model.GoalStatus
import com.yapp.love.domain.goal.model.RepeatCycle
import com.yapp.love.domain.goal.repository.GoalRepository
import com.yapp.love.domain.stamp.model.StampType
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDate

class GoalSchedulerTest : DescribeSpec({

    val goalRepository = mockk<GoalRepository>()
    val coupleInfoRepository = mockk<CoupleInfoRepository>()
    val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    val scheduler = GoalScheduler(
        goalRepository = goalRepository,
        coupleInfoRepository = coupleInfoRepository,
        notificationEventPublisher = eventPublisher,
    )

    beforeEach {
        clearAllMocks()
        every { goalRepository.updateNotStartedToInProgress(any()) } returns 0
        every { goalRepository.updateInProgressToCompleted(any()) } returns 0
        every { goalRepository.findGoalsEndingBefore(any()) } returns emptyList()
    }

    describe("processDaily") {

        context("종료 대상 목표가 없는 경우") {
            it("이벤트를 발행하지 않고 상태 업데이트만 수행한다") {
                every { goalRepository.findGoalsEndingBefore(any()) } returns emptyList()

                scheduler.processDaily()

                verify(exactly = 0) { eventPublisher.publishEvent(any()) }
                verify { goalRepository.updateNotStartedToInProgress(any()) }
                verify { goalRepository.updateInProgressToCompleted(any()) }
            }
        }

        context("종료 대상 목표가 있는 경우") {
            it("해당 커플에게 GoalEndedEvent를 발행한다") {
                val goal = Goal(
                    id = 1L,
                    coupleId = 100L,
                    name = "운동하기",
                    repeatCycle = RepeatCycle.DAILY,
                    repeatCount = 1,
                    startDate = LocalDate.now().minusDays(30),
                    goalStatus = GoalStatus.IN_PROGRESS,
                    icon = GoalIcon.ICON_EXERCISE,
                    stampType = StampType.CLOVER,
                )
                val coupleInfo = CoupleInfo(id = 100L, user1Id = 1L, user2Id = 2L, inviteCodeId = 1L)

                every { goalRepository.findGoalsEndingBefore(any()) } returns listOf(goal)
                every { coupleInfoRepository.findById(100L) } returns coupleInfo

                val eventSlot = slot<GoalEndedEvent>()
                every { eventPublisher.publishEvent(capture(eventSlot)) } just Runs

                scheduler.processDaily()

                val event = eventSlot.captured
                event.goalId shouldBe 1L
                event.goalName shouldBe "운동하기"
                event.user1Id shouldBe 1L
                event.user2Id shouldBe 2L
            }

            it("같은 커플의 목표가 여러 개면 각각 이벤트를 발행한다") {
                val goal1 = Goal(
                    id = 1L, coupleId = 100L, name = "운동하기",
                    repeatCycle = RepeatCycle.DAILY, repeatCount = 1,
                    startDate = LocalDate.now().minusDays(30),
                    goalStatus = GoalStatus.IN_PROGRESS, icon = GoalIcon.ICON_EXERCISE,
                    stampType = StampType.CLOVER,
                )
                val goal2 = Goal(
                    id = 2L, coupleId = 100L, name = "독서하기",
                    repeatCycle = RepeatCycle.DAILY, repeatCount = 1,
                    startDate = LocalDate.now().minusDays(30),
                    goalStatus = GoalStatus.IN_PROGRESS, icon = GoalIcon.ICON_EXERCISE,
                    stampType = StampType.CLOVER,
                )
                val coupleInfo = CoupleInfo(id = 100L, user1Id = 1L, user2Id = 2L, inviteCodeId = 1L)

                every { goalRepository.findGoalsEndingBefore(any()) } returns listOf(goal1, goal2)
                every { coupleInfoRepository.findById(100L) } returns coupleInfo

                scheduler.processDaily()

                verify(exactly = 2) { eventPublisher.publishEvent(any<GoalEndedEvent>()) }
            }

            it("서로 다른 커플의 목표가 있으면 각 커플에게 이벤트를 발행한다") {
                val goal1 = Goal(
                    id = 1L, coupleId = 100L, name = "운동하기",
                    repeatCycle = RepeatCycle.DAILY, repeatCount = 1,
                    startDate = LocalDate.now().minusDays(30),
                    goalStatus = GoalStatus.IN_PROGRESS, icon = GoalIcon.ICON_EXERCISE,
                    stampType = StampType.CLOVER,
                )
                val goal2 = Goal(
                    id = 2L, coupleId = 200L, name = "독서하기",
                    repeatCycle = RepeatCycle.DAILY, repeatCount = 1,
                    startDate = LocalDate.now().minusDays(30),
                    goalStatus = GoalStatus.IN_PROGRESS, icon = GoalIcon.ICON_EXERCISE,
                    stampType = StampType.CLOVER,
                )
                every { goalRepository.findGoalsEndingBefore(any()) } returns listOf(goal1, goal2)
                every { coupleInfoRepository.findById(100L) } returns CoupleInfo(id = 100L, user1Id = 1L, user2Id = 2L, inviteCodeId = 1L)
                every { coupleInfoRepository.findById(200L) } returns CoupleInfo(id = 200L, user1Id = 3L, user2Id = 4L, inviteCodeId = 2L)

                scheduler.processDaily()

                verify(exactly = 2) { eventPublisher.publishEvent(any<GoalEndedEvent>()) }
            }
        }

        context("커플 정보를 찾을 수 없는 경우") {
            it("해당 목표의 이벤트는 발행하지 않는다") {
                val goal = Goal(
                    id = 1L, coupleId = 100L, name = "운동하기",
                    repeatCycle = RepeatCycle.DAILY, repeatCount = 1,
                    startDate = LocalDate.now().minusDays(30),
                    goalStatus = GoalStatus.IN_PROGRESS, icon = GoalIcon.ICON_EXERCISE,
                    stampType = StampType.CLOVER,
                )
                every { goalRepository.findGoalsEndingBefore(any()) } returns listOf(goal)
                every { coupleInfoRepository.findById(100L) } returns null

                scheduler.processDaily()

                verify(exactly = 0) { eventPublisher.publishEvent(any()) }
            }
        }

        context("상태 업데이트") {
            it("항상 NOT_STARTED→IN_PROGRESS, IN_PROGRESS→COMPLETED 업데이트를 수행한다") {
                every { goalRepository.updateNotStartedToInProgress(any()) } returns 3
                every { goalRepository.updateInProgressToCompleted(any()) } returns 2

                scheduler.processDaily()

                verify { goalRepository.updateNotStartedToInProgress(any()) }
                verify { goalRepository.updateInProgressToCompleted(any()) }
            }
        }
    }
})