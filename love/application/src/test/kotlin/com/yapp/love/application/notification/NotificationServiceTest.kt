package com.yapp.love.application.notification

import com.yapp.love.application.notification.port.FcmPushService
import com.yapp.love.domain.notification.NotificationRepository
import com.yapp.love.domain.notification.model.Notification
import com.yapp.love.domain.notification.model.NotificationType
import com.yapp.love.globalutils.exception.GlobalException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*

class NotificationServiceTest : DescribeSpec({

    val notificationRepository = mockk<NotificationRepository>()
    val notificationSettingService = mockk<NotificationSettingService>()
    val fcmPushService = mockk<FcmPushService>()

    val notificationService = NotificationService(
        notificationRepository = notificationRepository,
        notificationSettingService = notificationSettingService,
        fcmPushService = fcmPushService,
    )

    val userId = 1L

    beforeEach {
        clearAllMocks()
    }

    describe("getNotifications") {
        it("사용자의 알림 목록을 최신순으로 반환한다") {
            val notifications = listOf(
                Notification.create(userId = userId, type = NotificationType.POKE, title = "찌르기", body = "body1"),
                Notification.create(userId = userId, type = NotificationType.GOAL_ENDED, title = "종료", body = "body2"),
            )
            every { notificationRepository.findByUserIdOrderByCreatedAtDesc(userId) } returns notifications

            val result = notificationService.getNotifications(userId)

            result shouldBe notifications
            verify { notificationRepository.findByUserIdOrderByCreatedAtDesc(userId) }
        }
    }

    describe("markAsRead") {
        val notificationId = 10L

        context("본인의 알림인 경우") {
            it("읽음 처리하고 저장한다") {
                val notification = Notification.create(
                    userId = userId,
                    type = NotificationType.POKE,
                    title = "test",
                    body = "body",
                )
                every { notificationRepository.findById(notificationId) } returns notification
                every { notificationRepository.save(notification) } returns notification

                notificationService.markAsRead(userId, notificationId)

                notification.isRead shouldBe true
                verify { notificationRepository.save(notification) }
            }
        }

        context("알림이 존재하지 않는 경우") {
            it("예외가 발생한다") {
                every { notificationRepository.findById(notificationId) } returns null

                shouldThrow<GlobalException> {
                    notificationService.markAsRead(userId, notificationId)
                }
            }
        }

        context("다른 사용자의 알림인 경우") {
            it("예외가 발생한다") {
                val otherUserNotification = Notification.create(
                    userId = 999L,
                    type = NotificationType.POKE,
                    title = "test",
                    body = "body",
                )
                every { notificationRepository.findById(notificationId) } returns otherUserNotification

                shouldThrow<IllegalArgumentException> {
                    notificationService.markAsRead(userId, notificationId)
                }
            }
        }
    }

    describe("markAllAsRead") {
        it("읽지 않은 알림만 읽음 처리한다") {
            val unread = Notification.create(userId = userId, type = NotificationType.POKE, title = "t1", body = "b1")
            val alreadyRead = Notification.create(userId = userId, type = NotificationType.POKE, title = "t2", body = "b2")
            alreadyRead.markAsRead()

            every { notificationRepository.findByUserId(userId) } returns listOf(unread, alreadyRead)
            every { notificationRepository.save(any()) } answers { firstArg() }

            notificationService.markAllAsRead(userId)

            unread.isRead shouldBe true
            verify(exactly = 1) { notificationRepository.save(unread) }
            verify(exactly = 0) { notificationRepository.save(alreadyRead) }
        }

        it("읽지 않은 알림이 없으면 저장하지 않는다") {
            every { notificationRepository.findByUserId(userId) } returns emptyList()

            notificationService.markAllAsRead(userId)

            verify(exactly = 0) { notificationRepository.save(any()) }
        }
    }

    describe("sendNotification") {
        val targetUserId = 2L

        context("푸시 설정이 켜져 있는 경우") {
            it("알림을 저장하고 FCM 푸시를 전송한다") {
                val savedNotification = Notification(
                    id = 100L,
                    userId = targetUserId,
                    type = NotificationType.POKE,
                    title = "철수님이 '운동하기' 찔렀어요",
                    body = "아직이신가요? 철수님이 궁금해 해요",
                )
                every { notificationRepository.save(any()) } returns savedNotification
                every { notificationSettingService.shouldSendPush(targetUserId, NotificationType.POKE) } returns true
                every { fcmPushService.sendPushToUser(any(), any(), any(), any()) } returns true

                notificationService.sendNotification(
                    targetUserId = targetUserId,
                    type = NotificationType.POKE,
                    titleArgs = arrayOf("철수", "운동하기"),
                    bodyArgs = arrayOf("철수"),
                    deepLinkParams = mapOf("goalId" to "10"),
                )

                verify(exactly = 2) { notificationRepository.save(any()) }
                verify {
                    fcmPushService.sendPushToUser(
                        userId = targetUserId,
                        title = "철수님이 '운동하기' 찔렀어요",
                        body = "아직이신가요? 철수님이 궁금해 해요",
                        deepLink = match { it != null && it.contains("notificationId=100") && it.contains("goalId=10") },
                    )
                }
            }
        }

        context("푸시 설정이 꺼져 있는 경우") {
            it("알림은 저장하지만 FCM 푸시는 전송하지 않는다") {
                val savedNotification = Notification(
                    id = 100L,
                    userId = targetUserId,
                    type = NotificationType.POKE,
                    title = "철수님이 '운동하기' 찔렀어요",
                    body = "아직이신가요? 철수님이 궁금해 해요",
                )
                every { notificationRepository.save(any()) } returns savedNotification
                every { notificationSettingService.shouldSendPush(targetUserId, NotificationType.POKE) } returns false

                notificationService.sendNotification(
                    targetUserId = targetUserId,
                    type = NotificationType.POKE,
                    titleArgs = arrayOf("철수", "운동하기"),
                    bodyArgs = arrayOf("철수"),
                )

                verify(exactly = 2) { notificationRepository.save(any()) }
                verify(exactly = 0) { fcmPushService.sendPushToUser(any(), any(), any(), any()) }
            }
        }

        context("FCM 전송이 실패한 경우") {
            it("예외 없이 정상 종료된다") {
                val savedNotification = Notification(
                    id = 100L,
                    userId = targetUserId,
                    type = NotificationType.POKE,
                    title = "t",
                    body = "b",
                )
                every { notificationRepository.save(any()) } returns savedNotification
                every { notificationSettingService.shouldSendPush(targetUserId, NotificationType.POKE) } returns true
                every { fcmPushService.sendPushToUser(any(), any(), any(), any()) } returns false

                notificationService.sendNotification(
                    targetUserId = targetUserId,
                    type = NotificationType.POKE,
                    titleArgs = arrayOf("철수", "운동하기"),
                    bodyArgs = arrayOf("철수"),
                )

                verify { fcmPushService.sendPushToUser(any(), any(), any(), any()) }
            }
        }
    }
})