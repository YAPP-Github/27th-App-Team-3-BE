package com.yapp.love.domain.notification.model

import com.yapp.love.domain.common.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "notifications",
    indexes = [
        Index(name = "idx_notifications_user_read", columnList = "user_id, is_read, created_at"),
    ],
)
class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    val type: NotificationType,
    @Column(nullable = false)
    val title: String,
    @Column(nullable = false, length = 500)
    val body: String,
    @Column(name = "deep_link", length = 200)
    var deepLink: String? = null,
    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false,
    @Column(name = "read_at")
    var readAt: LocalDateTime? = null,
) : BaseEntity() {
    fun markAsRead() {
        if (!isRead) {
            isRead = true
            readAt = LocalDateTime.now()
        }
    }

    companion object {
        fun create(
            userId: Long,
            type: NotificationType,
            title: String,
            body: String,
            deepLink: String? = null,
        ) = Notification(
            userId = userId,
            type = type,
            title = title,
            body = body,
            deepLink = deepLink,
        )
    }
}

enum class NotificationType(
    val titleTemplate: String,
    val bodyTemplate: String,
    val action: String,
) {
    PARTNER_CONNECTED(
        titleTemplate = "%s님과 연결됐어요",
        bodyTemplate = "목표 등록하고 바로 keep it up!",
        action = "partner-connected",
    ),

    POKE(
        titleTemplate = "%s님이 '%s' 찔렀어요",
        bodyTemplate = "아직이신가요? %s님이 궁금해 해요",
        action = "poke",
    ),

    GOAL_COMPLETED(
        titleTemplate = "%s님이 '%s' 다 했나봐요",
        bodyTemplate = "%s님의 새로운 사진 보러 가기",
        action = "goal-completed",
    ),

    REACTION(
        titleTemplate = "%s님이 반응을 남겼어요",
        bodyTemplate = "%s님이 남긴 반응 보러가기",
        action = "reaction",
    ),

    DAILY_GOAL_ACHIEVED(
        titleTemplate = "keep it luv 완료!",
        bodyTemplate = "오늘 하루도 키피럽한 서로에게 박수를",
        action = "daily-goal-achieved",
    ),

    GOAL_ENDED(
        titleTemplate = "%s이 드디어 종료됐어요",
        bodyTemplate = "그동안 수고했어요",
        action = "goal-ended",
    ),

    MARKETING(
        titleTemplate = "%s",
        bodyTemplate = "%s",
        action = "marketing",
    ),
    ;

    fun formatTitle(vararg args: Any): String = titleTemplate.format(*args)

    fun formatBody(vararg args: Any): String = bodyTemplate.format(*args)

    fun makeDeepLink(params: Map<String, String> = emptyMap()): String {
        if (params.isEmpty()) return "twix://notification/$action"
        val query = params.entries.joinToString("&") { "${it.key}=${it.value}" }
        return "twix://notification/$action?$query"
    }
}
