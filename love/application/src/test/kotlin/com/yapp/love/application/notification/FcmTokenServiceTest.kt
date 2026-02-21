package com.yapp.love.application.notification

import com.yapp.love.application.notification.port.FcmPushService
import com.yapp.love.domain.notification.FcmTokenRepository
import com.yapp.love.domain.notification.model.FcmToken
import com.yapp.love.domain.notification.model.NotificationSetting
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*

class FcmTokenServiceTest : DescribeSpec({

    val fcmTokenRepository = mockk<FcmTokenRepository>()
    val fcmPushService = mockk<FcmPushService>(relaxed = true)
    val notificationSettingService = mockk<NotificationSettingService>(relaxed = true)

    val fcmTokenService = FcmTokenService(
        fcmTokenRepository = fcmTokenRepository,
        fcmPushService = fcmPushService,
        notificationSettingService = notificationSettingService,
    )

    val userId = 1L
    val token = "fcm-token-abc"
    val deviceId = "device-123"

    beforeEach {
        clearAllMocks()
        every { fcmTokenRepository.deleteByTokenAndUserIdNot(any(), any()) } just Runs
        every { notificationSettingService.getSetting(any()) } returns NotificationSetting.create(userId = userId, isMarketingPushEnabled = true)
    }

    describe("registerToken") {

        context("해당 디바이스의 토큰이 이미 존재하는 경우") {
            it("기존 토큰을 업데이트한다") {
                val existingToken = FcmToken.create(userId = userId, token = "old-token", deviceId = deviceId)
                every { fcmTokenRepository.findByUserIdAndDeviceId(userId, deviceId) } returns existingToken
                every { fcmTokenRepository.save(existingToken) } returns existingToken

                fcmTokenService.registerToken(userId, token, deviceId)

                existingToken.token shouldBe token
                verify { fcmTokenRepository.save(existingToken) }
                verify(exactly = 0) { fcmTokenRepository.save(match<FcmToken> { it !== existingToken }) }
            }
        }

        context("해당 디바이스의 토큰이 존재하지 않는 경우") {
            it("새 토큰을 생성하여 저장한다") {
                every { fcmTokenRepository.findByUserIdAndDeviceId(userId, deviceId) } returns null
                every { fcmTokenRepository.save(any()) } answers { firstArg() }

                fcmTokenService.registerToken(userId, token, deviceId)

                verify {
                    fcmTokenRepository.save(match<FcmToken> {
                        it.userId == userId &&
                            it.token == token &&
                            it.deviceId == deviceId
                    })
                }
            }
        }
    }

    describe("registerToken - 마케팅 구독") {

        beforeEach {
            every { fcmTokenRepository.findByUserIdAndDeviceId(userId, deviceId) } returns null
            every { fcmTokenRepository.save(any()) } answers { firstArg() }
        }

        context("마케팅 푸시가 켜져 있는 경우") {
            it("마케팅 토픽을 구독한다") {
                every { notificationSettingService.getSetting(userId) } returns
                    NotificationSetting.create(userId = userId, isMarketingPushEnabled = true)

                fcmTokenService.registerToken(userId, token, deviceId)

                verify(exactly = 1) { fcmPushService.subscribeToTopic(token, FcmTokenService.MARKETING_TOPIC) }
            }
        }

        context("마케팅 푸시가 꺼져 있는 경우") {
            it("마케팅 토픽을 구독하지 않는다") {
                every { notificationSettingService.getSetting(userId) } returns
                    NotificationSetting.create(userId = userId, isMarketingPushEnabled = false)

                fcmTokenService.registerToken(userId, token, deviceId)

                verify(exactly = 0) { fcmPushService.subscribeToTopic(any(), any()) }
            }
        }

        context("알림 설정 조회가 실패하는 경우") {
            it("기본값 true로 처리하여 마케팅 토픽을 구독한다") {
                every { notificationSettingService.getSetting(userId) } throws RuntimeException("설정 조회 실패")

                fcmTokenService.registerToken(userId, token, deviceId)

                verify(exactly = 1) { fcmPushService.subscribeToTopic(token, FcmTokenService.MARKETING_TOPIC) }
            }
        }
    }
})