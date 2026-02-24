package com.yapp.love.application.notification

import com.yapp.love.domain.notification.NotificationSettingRepository
import com.yapp.love.domain.notification.model.NotificationSetting
import com.yapp.love.domain.notification.model.NotificationType
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*

class NotificationSettingServiceTest : DescribeSpec({

    val notificationSettingRepository = mockk<NotificationSettingRepository>()

    val service = NotificationSettingService(
        notificationSettingRepository = notificationSettingRepository,
    )

    val userId = 1L

    beforeEach {
        clearAllMocks()
    }

    describe("initSetting") {
        it("알림 설정을 생성하고 저장한다") {
            every { notificationSettingRepository.findByUserId(userId) } returns null
            every { notificationSettingRepository.save(any()) } answers { firstArg() }

            val result = service.initSetting(
                userId = userId,
                isPokePushEnabled = true,
                isMarketingPushEnabled = false,
                isNightPushEnabled = true,
            )

            result.userId shouldBe userId
            result.isPokePushEnabled shouldBe true
            result.isMarketingPushEnabled shouldBe false
            result.isNightPushEnabled shouldBe true
            verify { notificationSettingRepository.save(any()) }
        }

        context("설정이 이미 존재하는 경우") {
            it("기존 설정을 업데이트한다") {
                val existing = NotificationSetting.create(
                    userId = userId,
                    isPokePushEnabled = false,
                    isMarketingPushEnabled = false,
                    isNightPushEnabled = false,
                )
                every { notificationSettingRepository.findByUserId(userId) } returns existing
                every { notificationSettingRepository.save(any()) } answers { firstArg() }

                val result = service.initSetting(
                    userId = userId,
                    isPokePushEnabled = true,
                    isMarketingPushEnabled = true,
                    isNightPushEnabled = true,
                )

                result.isPokePushEnabled shouldBe true
                result.isMarketingPushEnabled shouldBe true
                result.isNightPushEnabled shouldBe true
                verify(exactly = 1) { notificationSettingRepository.save(any()) }
            }
        }
    }

    describe("getSetting") {
        context("설정이 존재하는 경우") {
            it("알림 설정을 반환한다") {
                val setting = NotificationSetting.create(userId = userId)
                every { notificationSettingRepository.findByUserId(userId) } returns setting

                val result = service.getSetting(userId)

                result shouldBe setting
            }
        }

        context("설정이 존재하지 않는 경우") {
            it("기본값(전부 false)으로 설정을 생성하고 반환한다") {
                every { notificationSettingRepository.findByUserId(userId) } returns null
                every { notificationSettingRepository.save(any()) } answers { firstArg() }

                val result = service.getSetting(userId)

                result.isPokePushEnabled shouldBe false
                result.isMarketingPushEnabled shouldBe false
                result.isNightPushEnabled shouldBe false
                verify { notificationSettingRepository.save(any()) }
            }
        }
    }

    describe("updatePokePush") {
        it("찌르기 푸시 설정을 변경한다") {
            val setting = NotificationSetting.create(userId = userId, isPokePushEnabled = false)
            every { notificationSettingRepository.findByUserId(userId) } returns setting
            every { notificationSettingRepository.save(any()) } answers { firstArg() }

            val result = service.updatePokePush(userId, true)

            result.isPokePushEnabled shouldBe true
        }

        context("설정이 존재하지 않는 경우") {
            it("기본값으로 설정을 생성한 뒤 변경한다") {
                every { notificationSettingRepository.findByUserId(userId) } returns null
                every { notificationSettingRepository.save(any()) } answers { firstArg() }

                val result = service.updatePokePush(userId, true)

                result.isPokePushEnabled shouldBe true
            }
        }
    }

    describe("updateMarketingPush") {
        it("마케팅 푸시 설정을 변경한다") {
            val setting = NotificationSetting.create(userId = userId, isMarketingPushEnabled = false)
            every { notificationSettingRepository.findByUserId(userId) } returns setting
            every { notificationSettingRepository.save(any()) } answers { firstArg() }

            val result = service.updateMarketingPush(userId, true)

            result.isMarketingPushEnabled shouldBe true
            verify { notificationSettingRepository.save(any()) }
        }
    }

    describe("updateNightPush") {
        it("야간 푸시 설정을 변경한다") {
            val setting = NotificationSetting.create(userId = userId, isNightPushEnabled = false)
            every { notificationSettingRepository.findByUserId(userId) } returns setting
            every { notificationSettingRepository.save(any()) } answers { firstArg() }

            val result = service.updateNightPush(userId, true)

            result.isNightPushEnabled shouldBe true
        }
    }

    describe("shouldSendPush") {

        context("설정이 존재하지 않는 경우") {
            it("false를 반환한다") {
                every { notificationSettingRepository.findByUserId(userId) } returns null

                service.shouldSendPush(userId, NotificationType.POKE) shouldBe false
            }
        }

        context("POKE 타입이고 찌르기 푸시가 꺼져 있는 경우") {
            it("false를 반환한다") {
                val setting = NotificationSetting.create(userId = userId, isPokePushEnabled = false)
                every { notificationSettingRepository.findByUserId(userId) } returns setting

                service.shouldSendPush(userId, NotificationType.POKE) shouldBe false
            }
        }

        context("타입별 설정이 켜져 있는 경우") {
            it("true를 반환한다") {
                val setting = NotificationSetting.create(userId = userId, isPokePushEnabled = true)
                every { notificationSettingRepository.findByUserId(userId) } returns setting

                service.shouldSendPush(userId, NotificationType.POKE) shouldBe true
            }
        }

        context("GOAL_COMPLETED 등 타입별 제한이 없는 경우") {
            it("설정이 존재하면 true를 반환한다") {
                val setting = NotificationSetting.create(userId = userId)
                every { notificationSettingRepository.findByUserId(userId) } returns setting

                service.shouldSendPush(userId, NotificationType.GOAL_COMPLETED) shouldBe true
            }
        }
    }
})