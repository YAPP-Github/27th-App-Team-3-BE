package com.yapp.love.domain.photolog.model

import com.yapp.love.domain.common.BaseEntity
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(
    name = "photo_log",
    uniqueConstraints = [
        // 한 사용자가 같은 목표를 같은 날짜에 중복 인증 방지
        UniqueConstraint(
            name = "uk_photo_log_goal_user_date",
            columnNames = ["goal_id", "user_id", "verification_date"]
        )
    ],
    indexes = [
        // 목표별 날짜별 인증 조회 (가장 빈번: 홈 화면)
        Index(name = "idx_photo_log_goal_date", columnList = "goal_id, verification_date"),
    ]
)
class Photolog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "goal_id", nullable = false)
    val goalId: Long,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "verification_date", nullable = false)
    val verificationDate: LocalDate,

    @Column(name = "uploaded_at", nullable = false)
    val uploadedAt: Instant,

    @Column(name = "image_url", nullable = false)
    var imageUrl: String,

    @Column(length = 30)
    var comment: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    var reaction: ReactionType? = null,
) : BaseEntity()
