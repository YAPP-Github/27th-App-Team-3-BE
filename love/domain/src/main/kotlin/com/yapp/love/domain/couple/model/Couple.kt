package com.yapp.love.domain.couple.model

import com.yapp.love.domain.common.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "couple_info")
class CoupleInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user1_id", nullable = false)
    val user1Id: Long,

    @Column(name = "user2_id", nullable = false)
    val user2Id: Long,

    @Column(name = "invite_code_id", nullable = false, unique = true)
    val inviteCodeId: Long,

    @Column(name = "anniversary_date")
    var anniversaryDate: LocalDate? = null,
) : BaseEntity() {

    fun setAnniversary(date: LocalDate) {
        anniversaryDate = date
    }

    companion object {
        fun create(user1Id: Long, user2Id: Long, inviteCodeId: Long) = CoupleInfo(
            user1Id = user1Id,
            user2Id = user2Id,
            inviteCodeId = inviteCodeId,
        )
    }
}
