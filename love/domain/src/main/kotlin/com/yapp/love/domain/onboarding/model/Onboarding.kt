package com.yapp.love.domain.onboarding.model

import com.yapp.love.domain.common.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "invite_codes")
class InviteCodes(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true, length = 10)
    val code: String,

    @Column(name = "creator_id", nullable = false)
    val creatorId: Long,

    @Column(name = "used_at")
    var usedAt: LocalDateTime? = null,

    @Column(name = "used_by_id")
    var usedById: Long? = null,
) : BaseEntity() {

    fun use(usedUserId: Long) {
        require(creatorId != usedUserId) { "자신의 초대 코드는 사용할 수 없습니다." }
        check(usedAt != null) { "이미 사용된 초대 코드입니다." }

        usedAt = LocalDateTime.now()
        usedById = usedUserId
    }
}

@Entity
@Table(name = "user_onboarding_info")
class UserOnboardingInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false, unique = true)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: OnboardingStatus = OnboardingStatus.COUPLE_CONNECTION,

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null,
) : BaseEntity()

enum class OnboardingStatus {
    TERMS_AGREEMENT,
    COUPLE_CONNECTION,
    PROFILE_SETUP,
    ANNIVERSARY_SETUP,
    COMPLETED,
}
