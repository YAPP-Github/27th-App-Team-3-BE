package com.yapp.love.domain.stamp.model

import com.yapp.love.domain.common.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "stamp_history",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_stamp_history_photolog",
            columnNames = ["photolog_id"],
        ),
    ],
    indexes = [
        Index(name = "idx_stamp_history_goal_user_date", columnList = "goal_id, user_id, stamp_date"),
    ],
)
class StampHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "photolog_id", nullable = false)
    val photologId: Long,
    @Column(name = "goal_id", nullable = false)
    val goalId: Long,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Column(name = "stamp_date", nullable = false)
    val stampDate: LocalDate,
    @Enumerated(EnumType.STRING)
    @Column(name = "stamp_color", nullable = false, length = 20)
    val stampColor: StampColor,
) : BaseEntity() {
    companion object {
        fun of(
            photologId: Long,
            goalId: Long,
            userId: Long,
            stampDate: LocalDate,
        ): StampHistory {
            return StampHistory(
                photologId = photologId,
                goalId = goalId,
                userId = userId,
                stampDate = stampDate,
                stampColor = StampColor.entries.random(),
            )
        }
    }
}
