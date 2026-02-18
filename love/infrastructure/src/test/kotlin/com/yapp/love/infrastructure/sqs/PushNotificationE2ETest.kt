package com.yapp.love.infrastructure.sqs

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.yapp.love.domain.notification.FcmTokenRepository
import com.yapp.love.domain.notification.model.FcmToken
import com.yapp.love.infrastructure.sqs.consumer.PushNotificationConsumer
import com.yapp.love.infrastructure.sqs.message.PushNotificationMessage
import com.yapp.love.infrastructure.sqs.producer.PushNotificationProducer
import io.awspring.cloud.sqs.config.SqsBootstrapConfiguration
import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory
import io.awspring.cloud.sqs.operations.SqsTemplate
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import java.util.concurrent.TimeUnit

@Testcontainers
@SpringBootTest( // context for E2E test
    classes = [
        JacksonAutoConfiguration::class,
        PushNotificationProducer::class,
        PushNotificationConsumer::class,
        PushNotificationE2ETest.SqsTestConfig::class,
    ],
)
class PushNotificationE2ETest {

    @Autowired
    lateinit var producer: PushNotificationProducer

    @Autowired
    lateinit var firebaseMessaging: FirebaseMessaging

    @Autowired
    lateinit var fcmTokenRepository: FcmTokenRepository

    @BeforeEach
    fun setUp() {
        clearMocks(firebaseMessaging, fcmTokenRepository)
    }

    @Test
    fun `메시지 발행부터 FCM 전송까지 전체 흐름이 동작한다`() {
        val userId = 1L
        val token = FcmToken.create(userId = userId, token = "test-fcm-token", deviceId = "device-1")

        every { fcmTokenRepository.findByUserId(userId) } returns listOf(token)
        every { firebaseMessaging.send(any<Message>()) } returns "mock-message-id"

        producer.sendPushNotification(
            PushNotificationMessage(
                userId = userId,
                title = "테스트 알림",
                body = "테스트 본문",
                deepLink = "twix://notification/poke?goalId=10",
            ),
        )

        val fcmSent = awaitVerification {
            verify(atLeast = 1) { firebaseMessaging.send(any<Message>()) }
        }
        assertTrue(fcmSent, "Consumer가 메시지를 수신하여 FCM을 전송해야 합니다")
        verify { fcmTokenRepository.findByUserId(userId) }
    }

    @Test
    fun `FCM 토큰이 없으면 FCM 전송을 건너뛴다`() {
        val userId = 2L

        every { fcmTokenRepository.findByUserId(userId) } returns emptyList()

        producer.sendPushNotification(
            PushNotificationMessage(
                userId = userId,
                title = "테스트 알림",
                body = "테스트 본문",
            ),
        )

        val consumed = awaitVerification {
            verify(atLeast = 1) { fcmTokenRepository.findByUserId(userId) }
        }
        assertTrue(consumed, "Consumer가 메시지를 수신해야 합니다")
        verify(exactly = 0) { firebaseMessaging.send(any<Message>()) }
    }

    private fun awaitVerification(
        timeoutSeconds: Long = 15,
        block: () -> Unit,
    ): Boolean {
        val deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSeconds)
        while (System.currentTimeMillis() < deadline) {
            try {
                block()
                return true
            } catch (_: AssertionError) {
                Thread.sleep(500)
            }
        }
        return false
    }

    companion object {
        private const val QUEUE_NAME = "twix-notification-test"

        @Container
        val localstack: LocalStackContainer = LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.4"),
        ).withServices(LocalStackContainer.Service.SQS)

        private fun buildSqsClient(): SqsAsyncClient {
            return SqsAsyncClient.builder()
                .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.SQS))
                .region(Region.of(localstack.region))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(localstack.accessKey, localstack.secretKey),
                    ),
                )
                .build()
        }

        @JvmStatic
        @DynamicPropertySource
        @Suppress("unused")
        fun overrideProperties(registry: DynamicPropertyRegistry) {
            val sqsClient = buildSqsClient()
            sqsClient.createQueue { it.queueName(QUEUE_NAME) }.get()
            val queueUrl = sqsClient.getQueueUrl { it.queueName(QUEUE_NAME) }.get().queueUrl()
            sqsClient.close()

            registry.add("sqs.queue.notification") { queueUrl }
        }
    }

    @Configuration
    @Import(SqsBootstrapConfiguration::class)
    class SqsTestConfig {
        @Bean
        fun sqsAsyncClient(): SqsAsyncClient = buildSqsClient()

        @Bean
        fun sqsTemplate(sqsAsyncClient: SqsAsyncClient): SqsTemplate =
            SqsTemplate.newTemplate(sqsAsyncClient)

        @Bean
        fun defaultSqsListenerContainerFactory(sqsAsyncClient: SqsAsyncClient): SqsMessageListenerContainerFactory<Any> =
            SqsMessageListenerContainerFactory.builder<Any>()
                .sqsAsyncClient(sqsAsyncClient)
                .build()

        @Bean
        fun firebaseMessaging(): FirebaseMessaging = mockk(relaxed = true)

        @Bean
        fun fcmTokenRepository(): FcmTokenRepository = mockk(relaxed = true)
    }
}
