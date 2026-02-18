package com.yapp.love.application.poke

import com.yapp.love.application.notification.event.PokedEvent
import com.yapp.love.domain.couple.CoupleInfoRepository
import com.yapp.love.domain.couple.model.CoupleInfo
import com.yapp.love.domain.goal.model.Goal
import com.yapp.love.domain.goal.model.GoalIcon
import com.yapp.love.domain.goal.model.GoalStatus
import com.yapp.love.domain.goal.model.RepeatCycle
import com.yapp.love.domain.goal.repository.GoalRepository
import com.yapp.love.domain.poke.PokeRepository
import com.yapp.love.domain.poke.model.Poke
import com.yapp.love.domain.user.UserAdditionInfoRepository
import com.yapp.love.domain.user.model.UserAdditionInfo
import com.yapp.love.globalutils.exception.GlobalException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDate

class PokeServiceTest : DescribeSpec({

    val pokeRepository = mockk<PokeRepository>()
    val coupleInfoRepository = mockk<CoupleInfoRepository>()
    val goalRepository = mockk<GoalRepository>()
    val userAdditionInfoRepository = mockk<UserAdditionInfoRepository>()
    val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    val pokeService = PokeService(
        pokeRepository = pokeRepository,
        coupleInfoRepository = coupleInfoRepository,
        goalRepository = goalRepository,
        userAdditionInfoRepository = userAdditionInfoRepository,
        notificationEventPublisher = eventPublisher,
    )

    val senderId = 1L
    val receiverId = 2L
    val coupleId = 100L
    val goalId = 10L

    val coupleInfo = CoupleInfo(
        id = coupleId,
        user1Id = senderId,
        user2Id = receiverId,
        inviteCodeId = 1L,
    )

    val goal = Goal(
        id = goalId,
        coupleId = coupleId,
        name = "운동하기",
        repeatCycle = RepeatCycle.DAILY,
        repeatCount = 1,
        startDate = LocalDate.of(2026, 1, 1),
        hasEndDate = true,
        endDate = LocalDate.of(2026, 3, 1),
        goalStatus = GoalStatus.IN_PROGRESS,
        icon = GoalIcon.ICON_EXERCISE,
    )

    beforeEach {
        clearAllMocks()
    }

    describe("poke") {

        context("정상적으로 찌르기 성공하는 경우") {
            beforeEach {
                every { coupleInfoRepository.findByUserId(senderId) } returns coupleInfo
                every { goalRepository.findById(goalId) } returns goal
                every { pokeRepository.save(any()) } returns mockk()
                every { userAdditionInfoRepository.findByUserId(senderId) } returns
                    UserAdditionInfo(userId = senderId, nickname = "철수")
            }

            it("찌르기가 저장된다") {
                pokeService.poke(senderId, goalId)

                verify {
                    pokeRepository.save(match<Poke> {
                        it.senderId == senderId &&
                            it.receiverId == receiverId &&
                            it.goalId == goalId
                    })
                }
            }

            it("상대방에게 PokedEvent가 발행된다") {
                pokeService.poke(senderId, goalId)

                verify {
                    eventPublisher.publishEvent(
                        PokedEvent(
                            targetUserId = receiverId,
                            senderNickname = "철수",
                            goalId = goalId,
                            goalName = "운동하기",
                        )
                    )
                }
            }

            it("닉네임이 없으면 '상대방'으로 이벤트가 발행된다") {
                every { userAdditionInfoRepository.findByUserId(senderId) } returns null

                pokeService.poke(senderId, goalId)

                verify {
                    eventPublisher.publishEvent(
                        PokedEvent(
                            targetUserId = receiverId,
                            senderNickname = "상대방",
                            goalId = goalId,
                            goalName = "운동하기",
                        )
                    )
                }
            }
        }

        context("커플 정보가 없는 경우") {
            it("예외가 발생한다") {
                every { coupleInfoRepository.findByUserId(senderId) } returns null

                shouldThrow<GlobalException> {
                    pokeService.poke(senderId, goalId)
                }
            }
        }

        context("목표를 찾을 수 없는 경우") {
            it("예외가 발생한다") {
                every { coupleInfoRepository.findByUserId(senderId) } returns coupleInfo
                every { goalRepository.findById(goalId) } returns null

                shouldThrow<GlobalException> {
                    pokeService.poke(senderId, goalId)
                }
            }
        }

        context("다른 커플의 목표인 경우") {
            it("예외가 발생한다") {
                val otherGoal = Goal(
                    id = goalId,
                    coupleId = 999L,
                    name = "운동하기",
                    repeatCycle = RepeatCycle.DAILY,
                    repeatCount = 1,
                    startDate = LocalDate.of(2026, 1, 1),
                    goalStatus = GoalStatus.IN_PROGRESS,
                    icon = GoalIcon.ICON_EXERCISE,
                )

                every { coupleInfoRepository.findByUserId(senderId) } returns coupleInfo
                every { goalRepository.findById(goalId) } returns otherGoal

                shouldThrow<GlobalException> {
                    pokeService.poke(senderId, goalId)
                }
            }
        }

        context("진행중이지 않은 목표인 경우") {
            it("예외가 발생한다") {
                val completedGoal = Goal(
                    id = goalId,
                    coupleId = coupleId,
                    name = "운동하기",
                    repeatCycle = RepeatCycle.DAILY,
                    repeatCount = 1,
                    startDate = LocalDate.of(2026, 1, 1),
                    goalStatus = GoalStatus.COMPLETED,
                    icon = GoalIcon.ICON_EXERCISE,
                )

                every { coupleInfoRepository.findByUserId(senderId) } returns coupleInfo
                every { goalRepository.findById(goalId) } returns completedGoal

                shouldThrow<GlobalException> {
                    pokeService.poke(senderId, goalId)
                }
            }
        }
    }
})