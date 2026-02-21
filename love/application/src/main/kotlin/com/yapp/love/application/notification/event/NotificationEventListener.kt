package com.yapp.love.application.notification.event

import com.yapp.love.application.notification.NotificationService
import com.yapp.love.application.notification.port.FcmPushService
import com.yapp.love.domain.couple.CoupleInfoRepository
import com.yapp.love.domain.goal.repository.GoalRepository
import com.yapp.love.domain.notification.model.NotificationType
import com.yapp.love.domain.user.UserAdditionInfoRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Component
class NotificationEventListener(
    private val notificationService: NotificationService,
    private val fcmPushService: FcmPushService,
    private val coupleInfoRepository: CoupleInfoRepository,
    private val userAdditionInfoRepository: UserAdditionInfoRepository,
    private val goalRepository: GoalRepository,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handlePartnerConnected(event: PartnerConnectedEvent) {
        val nickname = userAdditionInfoRepository.findByUserId(event.senderUserId)?.nickname ?: "상대방"
        notificationService.sendNotification(
            targetUserId = event.targetUserId,
            type = NotificationType.PARTNER_CONNECTED,
            titleArgs = arrayOf(nickname),
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handlePoked(event: PokedEvent) {
        notificationService.sendNotification(
            targetUserId = event.targetUserId,
            type = NotificationType.POKE,
            titleArgs = arrayOf(event.senderNickname, event.goalName),
            bodyArgs = arrayOf(event.senderNickname),
            deepLinkParams = mapOf(
                "goalId" to event.goalId.toString(),
                "date" to LocalDate.now().toString(),
            ),
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handlePhotologCreated(event: PhotologCreatedEvent) {
        val coupleInfo = coupleInfoRepository.findByUserId(event.userId)
        if (coupleInfo == null) {
            logger.warn { "커플 정보를 찾을 수 없음: userId=${event.userId}" }
            return
        }

        val partnerId = if (coupleInfo.user1Id == event.userId) coupleInfo.user2Id else coupleInfo.user1Id
        val nickname = userAdditionInfoRepository.findByUserId(event.userId)?.nickname ?: "상대방"
        val goal = goalRepository.findById(event.goalId)
        if (goal == null) {
            logger.warn { "목표를 찾을 수 없음: goalId=${event.goalId}" }
            return
        }

        notificationService.sendNotification(
            targetUserId = partnerId,
            type = NotificationType.GOAL_COMPLETED,
            titleArgs = arrayOf(nickname, goal.name),
            bodyArgs = arrayOf(nickname),
            deepLinkParams = mapOf(
                "goalId" to event.goalId.toString(),
                "date" to LocalDate.now().toString(),
            ),
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleGoalEnded(event: GoalEndedEvent) {
        val deepLinkParams = mapOf("goalId" to event.goalId.toString())
        listOf(event.user1Id, event.user2Id).forEach { targetUserId ->
            notificationService.sendNotification(
                targetUserId = targetUserId,
                type = NotificationType.GOAL_ENDED,
                titleArgs = arrayOf(event.goalName),
                deepLinkParams = deepLinkParams,
            )
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleReactionCreated(event: ReactionCreatedEvent) {
        val nickname = userAdditionInfoRepository.findByUserId(event.reactorUserId)?.nickname ?: "상대방"
        notificationService.sendNotification(
            targetUserId = event.photologOwnerId,
            type = NotificationType.REACTION,
            titleArgs = arrayOf(nickname),
            bodyArgs = arrayOf(nickname),
            deepLinkParams = mapOf(
                "goalId" to event.goalId.toString(),
                "date" to event.verificationDate.toString(),
            ),
        )
    }

    @Async("fcmTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleFcmPush(event: FcmPushEvent) {
        fcmPushService.sendPushToUser(
            userId = event.userId,
            title = event.title,
            body = event.body,
            deepLink = event.deepLink,
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleDailyGoalAchieved(event: DailyGoalAchievedEvent) {
        listOf(event.user1Id, event.user2Id).forEach { targetUserId ->
            notificationService.sendNotification(
                targetUserId = targetUserId,
                type = NotificationType.DAILY_GOAL_ACHIEVED,
                titleArgs = arrayOf(event.goalName),
            )
        }
    }
}