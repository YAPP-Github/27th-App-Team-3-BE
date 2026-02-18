package com.yapp.love.domain.poke.model

import com.yapp.love.domain.common.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "pokes",
    indexes = [
        Index(name = "idx_pokes_sender_goal", columnList = "sender_id, goal_id, created_at"),
    ],
)
class Poke(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "sender_id", nullable = false)
    val senderId: Long,
    @Column(name = "receiver_id", nullable = false)
    val receiverId: Long,
    @Column(name = "goal_id", nullable = false)
    val goalId: Long,
) : BaseEntity() {
    companion object {
        private const val COOLDOWN_HOURS = 3L

        fun create(
            senderId: Long,
            receiverId: Long,
            goalId: Long,
        ) = Poke(
            senderId = senderId,
            receiverId = receiverId,
            goalId = goalId,
        )

        fun canPoke(lastPokeTime: LocalDateTime?): Boolean {
            if (lastPokeTime == null) return true
            return LocalDateTime.now().isAfter(lastPokeTime.plusHours(COOLDOWN_HOURS))
        }
    }
}
