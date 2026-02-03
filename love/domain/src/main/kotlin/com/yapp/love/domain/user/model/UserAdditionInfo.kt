package com.yapp.love.domain.user.model

import com.yapp.love.domain.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "user_addition_info")
class UserAdditionInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false, unique = true)
    val userId: Long,

    @Column(nullable = false, length = 10)
    var nickname: String,
) : BaseEntity() {

    fun updateNickname(nickname: String) {
        this.nickname = nickname
    }

    companion object {
        fun create(userId: Long, nickname: String) = UserAdditionInfo(
            userId = userId,
            nickname = nickname,
        )
    }
}