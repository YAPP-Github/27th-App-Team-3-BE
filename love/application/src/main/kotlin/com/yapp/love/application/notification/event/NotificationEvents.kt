package com.yapp.love.application.notification.event

import java.time.LocalDate

data class PartnerConnectedEvent(
    val targetUserId: Long,
    val senderUserId: Long,
)

data class PokedEvent(
    val targetUserId: Long,
    val senderNickname: String,
    val goalId: Long,
    val goalName: String,
    val verificationDate: LocalDate,
)

data class PhotologCreatedEvent(
    val userId: Long,
    val goalId: Long,
)

data class GoalEndedEvent(
    val user1Id: Long,
    val user2Id: Long,
    val goalId: Long,
    val goalName: String,
)

data class ReactionCreatedEvent(
    val reactorUserId: Long,
    val photologOwnerId: Long,
    val goalId: Long,
    val verificationDate: LocalDate,
)

data class DailyGoalAchievedEvent(
    val user1Id: Long,
    val user2Id: Long,
    val goalName: String,
)

data class FcmPushEvent(
    val userId: Long,
    val title: String,
    val body: String,
    val deepLink: String,
)