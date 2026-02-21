package com.yapp.love.application.notification.event

import com.yapp.love.application.notification.NotificationService
import com.yapp.love.application.notification.port.FcmPushService
import com.yapp.love.domain.couple.CoupleInfoRepository
import com.yapp.love.domain.couple.model.CoupleInfo
import com.yapp.love.domain.goal.model.Goal
import com.yapp.love.domain.goal.model.GoalIcon
import com.yapp.love.domain.goal.model.GoalStatus
import com.yapp.love.domain.goal.model.RepeatCycle
import com.yapp.love.domain.goal.repository.GoalRepository
import com.yapp.love.domain.notification.model.NotificationType
import com.yapp.love.domain.user.UserAdditionInfoRepository
import com.yapp.love.domain.user.model.UserAdditionInfo
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.*
import java.time.LocalDate

class NotificationEventListenerTest : DescribeSpec({

    val notificationService = mockk<NotificationService>(relaxed = true)
    val fcmPushService = mockk<FcmPushService>(relaxed = true)
    val coupleInfoRepository = mockk<CoupleInfoRepository>()
    val userAdditionInfoRepository = mockk<UserAdditionInfoRepository>()
    val goalRepository = mockk<GoalRepository>()

    val listener = NotificationEventListener(
        notificationService = notificationService,
        fcmPushService = fcmPushService,
        coupleInfoRepository = coupleInfoRepository,
        userAdditionInfoRepository = userAdditionInfoRepository,
        goalRepository = goalRepository,
    )

    beforeEach {
        clearAllMocks()
    }

    describe("handlePartnerConnected") {
        it("PARTNER_CONNECTED 알림을 전송한다") {
            val event = PartnerConnectedEvent(targetUserId = 1L, senderUserId = 2L)
            every { userAdditionInfoRepository.findByUserId(2L) } returns
                mockk { every { nickname } returns "지수" }

            listener.handlePartnerConnected(event)

            verify {
                notificationService.sendNotification(
                    targetUserId = 1L,
                    type = NotificationType.PARTNER_CONNECTED,
                    titleArgs = arrayOf("지수"),
                )
            }
        }

        it("닉네임이 없으면 '상대방'으로 전송한다") {
            val event = PartnerConnectedEvent(targetUserId = 1L, senderUserId = 2L)
            every { userAdditionInfoRepository.findByUserId(2L) } returns null

            listener.handlePartnerConnected(event)

            verify {
                notificationService.sendNotification(
                    targetUserId = 1L,
                    type = NotificationType.PARTNER_CONNECTED,
                    titleArgs = arrayOf("상대방"),
                )
            }
        }

        it("예외 발생 시 메서드가 정상 종료된다") {
            val event = PartnerConnectedEvent(targetUserId = 1L, senderUserId = 2L)
            every { userAdditionInfoRepository.findByUserId(2L) } throws RuntimeException("DB 오류")

            listener.handlePartnerConnected(event) // 예외 전파 없이 종료되어야 함
        }
    }

    describe("handlePoked") {
        it("POKE 알림을 전송한다") {
            val event = PokedEvent(
                targetUserId = 2L,
                senderNickname = "철수",
                goalId = 10L,
                goalName = "운동하기",
            )

            listener.handlePoked(event)

            verify {
                notificationService.sendNotification(
                    targetUserId = 2L,
                    type = NotificationType.POKE,
                    titleArgs = arrayOf("철수", "운동하기"),
                    bodyArgs = arrayOf("철수"),
                    deepLinkParams = mapOf("goalId" to "10", "date" to LocalDate.now().toString()),
                )
            }
        }

        it("예외 발생 시 메서드가 정상 종료된다") {
            val event = PokedEvent(targetUserId = 2L, senderNickname = "철수", goalId = 10L, goalName = "운동하기")
            every { notificationService.sendNotification(any(), any(), any(), any(), any()) } throws RuntimeException("DB 오류")

            listener.handlePoked(event)
        }
    }

    describe("handlePhotologCreated") {
        val userId = 1L
        val partnerId = 2L
        val goalId = 10L

        val coupleInfo = CoupleInfo(
            id = 100L,
            user1Id = userId,
            user2Id = partnerId,
            inviteCodeId = 1L,
        )

        val goal = Goal(
            id = goalId,
            coupleId = 100L,
            name = "운동하기",
            repeatCycle = RepeatCycle.DAILY,
            repeatCount = 1,
            startDate = LocalDate.of(2026, 1, 1),
            goalStatus = GoalStatus.IN_PROGRESS,
            icon = GoalIcon.ICON_EXERCISE,
        )

        context("정상적인 경우") {
            it("상대방에게 GOAL_COMPLETED 알림을 전송한다") {
                every { coupleInfoRepository.findByUserId(userId) } returns coupleInfo
                every { userAdditionInfoRepository.findByUserId(userId) } returns
                    UserAdditionInfo(userId = userId, nickname = "철수")
                every { goalRepository.findById(goalId) } returns goal

                listener.handlePhotologCreated(PhotologCreatedEvent(userId = userId, goalId = goalId))

                verify {
                    notificationService.sendNotification(
                        targetUserId = partnerId,
                        type = NotificationType.GOAL_COMPLETED,
                        titleArgs = arrayOf("철수", "운동하기"),
                        bodyArgs = arrayOf("철수"),
                        deepLinkParams = mapOf("goalId" to "10", "date" to LocalDate.now().toString()),
                    )
                }
            }
        }

        context("user2Id가 이벤트 발행자인 경우") {
            it("user1Id에게 알림을 전송한다") {
                val reverseCouple = CoupleInfo(
                    id = 100L,
                    user1Id = partnerId,
                    user2Id = userId,
                    inviteCodeId = 1L,
                )
                every { coupleInfoRepository.findByUserId(userId) } returns reverseCouple
                every { userAdditionInfoRepository.findByUserId(userId) } returns
                    UserAdditionInfo(userId = userId, nickname = "철수")
                every { goalRepository.findById(goalId) } returns goal

                listener.handlePhotologCreated(PhotologCreatedEvent(userId = userId, goalId = goalId))

                verify {
                    notificationService.sendNotification(
                        targetUserId = partnerId,
                        type = NotificationType.GOAL_COMPLETED,
                        titleArgs = arrayOf("철수", "운동하기"),
                        bodyArgs = arrayOf("철수"),
                        deepLinkParams = mapOf("goalId" to "10", "date" to LocalDate.now().toString()),
                    )
                }
            }
        }

        context("닉네임이 없는 경우") {
            it("'상대방'으로 알림을 전송한다") {
                every { coupleInfoRepository.findByUserId(userId) } returns coupleInfo
                every { userAdditionInfoRepository.findByUserId(userId) } returns null
                every { goalRepository.findById(goalId) } returns goal

                listener.handlePhotologCreated(PhotologCreatedEvent(userId = userId, goalId = goalId))

                verify {
                    notificationService.sendNotification(
                        targetUserId = partnerId,
                        type = NotificationType.GOAL_COMPLETED,
                        titleArgs = arrayOf("상대방", "운동하기"),
                        bodyArgs = arrayOf("상대방"),
                        deepLinkParams = mapOf("goalId" to "10", "date" to LocalDate.now().toString()),
                    )
                }
            }
        }

        context("커플 정보가 없는 경우") {
            it("알림을 전송하지 않는다") {
                every { coupleInfoRepository.findByUserId(userId) } returns null

                listener.handlePhotologCreated(PhotologCreatedEvent(userId = userId, goalId = goalId))

                verify(exactly = 0) { notificationService.sendNotification(any(), any(), any(), any(), any()) }
            }
        }

        context("목표를 찾을 수 없는 경우") {
            it("알림을 전송하지 않는다") {
                every { coupleInfoRepository.findByUserId(userId) } returns coupleInfo
                every { userAdditionInfoRepository.findByUserId(userId) } returns
                    UserAdditionInfo(userId = userId, nickname = "철수")
                every { goalRepository.findById(goalId) } returns null

                listener.handlePhotologCreated(PhotologCreatedEvent(userId = userId, goalId = goalId))

                verify(exactly = 0) { notificationService.sendNotification(any(), any(), any(), any(), any()) }
            }
        }
    }

    describe("handleGoalEnded") {
        it("양쪽 사용자에게 GOAL_ENDED 알림을 전송한다") {
            val event = GoalEndedEvent(user1Id = 1L, user2Id = 2L, goalId = 10L, goalName = "운동하기")

            listener.handleGoalEnded(event)

            verify {
                notificationService.sendNotification(
                    targetUserId = 1L,
                    type = NotificationType.GOAL_ENDED,
                    titleArgs = arrayOf("운동하기"),
                    deepLinkParams = mapOf("goalId" to "10"),
                )
            }
            verify {
                notificationService.sendNotification(
                    targetUserId = 2L,
                    type = NotificationType.GOAL_ENDED,
                    titleArgs = arrayOf("운동하기"),
                    deepLinkParams = mapOf("goalId" to "10"),
                )
            }
        }

        it("user1 알림 전송 실패 시 user2에게는 정상 전송된다") {
            val event = GoalEndedEvent(user1Id = 1L, user2Id = 2L, goalId = 10L, goalName = "운동하기")
            every {
                notificationService.sendNotification(
                    targetUserId = 1L,
                    type = any(),
                    titleArgs = any(),
                    deepLinkParams = any(),
                )
            } throws RuntimeException("user1 DB 오류")

            listener.handleGoalEnded(event) // 예외 전파 없이 종료되어야 함

            verify {
                notificationService.sendNotification(
                    targetUserId = 2L,
                    type = NotificationType.GOAL_ENDED,
                    titleArgs = arrayOf("운동하기"),
                    deepLinkParams = mapOf("goalId" to "10"),
                )
            }
        }
    }

    describe("handleDailyGoalAchieved") {
        it("양쪽 사용자에게 DAILY_GOAL_ACHIEVED 알림을 전송한다") {
            val event = DailyGoalAchievedEvent(user1Id = 1L, user2Id = 2L, goalName = "운동하기")

            listener.handleDailyGoalAchieved(event)

            verify {
                notificationService.sendNotification(
                    targetUserId = 1L,
                    type = NotificationType.DAILY_GOAL_ACHIEVED,
                    titleArgs = arrayOf("운동하기"),
                )
            }
            verify {
                notificationService.sendNotification(
                    targetUserId = 2L,
                    type = NotificationType.DAILY_GOAL_ACHIEVED,
                    titleArgs = arrayOf("운동하기"),
                )
            }
        }

        it("user1 알림 전송 실패 시 user2에게는 정상 전송된다") {
            val event = DailyGoalAchievedEvent(user1Id = 1L, user2Id = 2L, goalName = "운동하기")
            every {
                notificationService.sendNotification(
                    targetUserId = 1L,
                    type = any(),
                    titleArgs = any(),
                )
            } throws RuntimeException("user1 DB 오류")

            listener.handleDailyGoalAchieved(event) // 예외 전파 없이 종료되어야 함

            verify {
                notificationService.sendNotification(
                    targetUserId = 2L,
                    type = NotificationType.DAILY_GOAL_ACHIEVED,
                    titleArgs = arrayOf("운동하기"),
                )
            }
        }
    }

    describe("handleReactionCreated") {
        it("포토로그 소유자에게 REACTION 알림을 전송한다") {
            val event = ReactionCreatedEvent(
                reactorUserId = 1L,
                photologOwnerId = 2L,
                goalId = 10L,
                verificationDate = LocalDate.of(2026, 2, 21),
            )
            every { userAdditionInfoRepository.findByUserId(1L) } returns
                UserAdditionInfo(userId = 1L, nickname = "철수")

            listener.handleReactionCreated(event)

            verify {
                notificationService.sendNotification(
                    targetUserId = 2L,
                    type = NotificationType.REACTION,
                    titleArgs = arrayOf("철수"),
                    bodyArgs = arrayOf("철수"),
                    deepLinkParams = mapOf("goalId" to "10", "date" to "2026-02-21"),
                )
            }
        }

        it("닉네임이 없으면 '상대방'으로 전송한다") {
            val event = ReactionCreatedEvent(
                reactorUserId = 1L,
                photologOwnerId = 2L,
                goalId = 10L,
                verificationDate = LocalDate.of(2026, 2, 21),
            )
            every { userAdditionInfoRepository.findByUserId(1L) } returns null

            listener.handleReactionCreated(event)

            verify {
                notificationService.sendNotification(
                    targetUserId = 2L,
                    type = NotificationType.REACTION,
                    titleArgs = arrayOf("상대방"),
                    bodyArgs = arrayOf("상대방"),
                    deepLinkParams = mapOf("goalId" to "10", "date" to "2026-02-21"),
                )
            }
        }
    }
})