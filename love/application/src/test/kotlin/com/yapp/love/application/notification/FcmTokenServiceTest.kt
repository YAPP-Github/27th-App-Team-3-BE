package com.yapp.love.application.notification

import com.yapp.love.application.notification.port.FcmPushService
import com.yapp.love.domain.notification.FcmTokenRepository
import com.yapp.love.domain.notification.model.FcmToken
import com.yapp.love.domain.notification.model.NotificationSetting
import com.yapp.love.globalutils.exception.GlobalException
import io.kotest.assertions.throwables.shouldThrow
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
        every { fcmTokenRepository.findByDeviceIdAndUserIdNot(any(), any()) } returns emptyList()
        every { fcmTokenRepository.deleteAll(any()) } just Runs
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

    describe("registerToken - 계정 전환") {

        context("같은 기기에 다른 유저의 토큰이 존재하는 경우") {
            it("이전 유저의 토큰을 unsubscribe 후 삭제한다") {
                val otherUserId = 99L
                val oldToken = FcmToken.create(userId = otherUserId, token = "old-token", deviceId = deviceId)
                every { fcmTokenRepository.findByDeviceIdAndUserIdNot(deviceId, userId) } returns listOf(oldToken)
                every { fcmTokenRepository.findByUserIdAndDeviceId(userId, deviceId) } returns null
                every { fcmTokenRepository.save(any()) } answers { firstArg() }

                fcmTokenService.registerToken(userId, token, deviceId)

                verifyOrder {
                    fcmPushService.unsubscribeFromTopic(oldToken.token, FcmTokenService.MARKETING_TOPIC)
                    fcmTokenRepository.deleteAll(listOf(oldToken))
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

        context("알림 설정이 존재하지 않는 경우") {
            it("기본값 false로 처리하여 마케팅 토픽을 구독하지 않는다") {
                every { notificationSettingService.getSetting(userId) } returns
                    NotificationSetting.create(userId = userId, isMarketingPushEnabled = false)

                fcmTokenService.registerToken(userId, token, deviceId)

                verify(exactly = 0) { fcmPushService.subscribeToTopic(any(), any()) }
            }
        }
    }

    describe("deleteToken") {

        context("토큰이 존재하지 않는 경우") {
            it("아무것도 하지 않는다") {
                every { fcmTokenRepository.findByUserIdAndToken(userId, token) } returns null

                fcmTokenService.deleteToken(userId, token)

                verify(exactly = 0) { fcmPushService.unsubscribeFromTopicOrThrow(any(), any()) }
                verify(exactly = 0) { fcmTokenRepository.delete(any()) }
            }
        }

        context("토큰이 존재하는 경우") {
            val fcmToken = FcmToken.create(userId = userId, token = token, deviceId = deviceId)

            it("unsubscribe 후 토큰을 삭제한다") {
                every { fcmTokenRepository.findByUserIdAndToken(userId, token) } returns fcmToken
                every { fcmTokenRepository.delete(fcmToken) } just Runs

                fcmTokenService.deleteToken(userId, token)

                verifyOrder {
                    fcmPushService.unsubscribeFromTopicOrThrow(token, FcmTokenService.MARKETING_TOPIC)
                    fcmTokenRepository.delete(fcmToken)
                }
            }

            it("unsubscribe 실패 시 예외를 던지고 토큰을 삭제하지 않는다") {
                every { fcmTokenRepository.findByUserIdAndToken(userId, token) } returns fcmToken
                every { fcmPushService.unsubscribeFromTopicOrThrow(any(), any()) } throws GlobalException(
                    com.yapp.love.globalutils.exception.GlobalErrorCode.FCM_UNSUBSCRIBE_FAILED
                )

                shouldThrow<GlobalException> {
                    fcmTokenService.deleteToken(userId, token)
                }

                verify(exactly = 0) { fcmTokenRepository.delete(any()) }
            }
        }
    }
})