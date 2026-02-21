package com.yapp.love.application.notification

import com.yapp.love.domain.notification.NotificationRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


class NotificationCleanupSchedulerTest : DescribeSpec({

    val notificationRepository = mockk<NotificationRepository>()
    val scheduler = NotificationCleanupScheduler(notificationRepository = notificationRepository)

    beforeEach { clearAllMocks() }

    describe("deleteOldNotifications") {
        it("14일 이전 기준으로 deleteCreatedBefore를 호출한다") {
            val thresholdSlot = slot<LocalDateTime>()
            every { notificationRepository.deleteCreatedBefore(capture(thresholdSlot)) } returns 5

            scheduler.deleteOldNotifications()

            val captured = thresholdSlot.captured
            val capturedDays = ChronoUnit.DAYS.between(captured, LocalDateTime.now())
            (capturedDays in 14L..15L) shouldBe true

            verify(exactly = 1) { notificationRepository.deleteCreatedBefore(any()) }
        }

        it("삭제 대상이 없어도 정상 종료된다") {
            every { notificationRepository.deleteCreatedBefore(any()) } returns 0

            scheduler.deleteOldNotifications()

            verify(exactly = 1) { notificationRepository.deleteCreatedBefore(any()) }
        }
    }
})