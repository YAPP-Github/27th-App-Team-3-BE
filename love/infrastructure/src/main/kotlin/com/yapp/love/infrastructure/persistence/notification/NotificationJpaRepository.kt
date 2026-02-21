package com.yapp.love.infrastructure.persistence.notification

import com.yapp.love.domain.notification.NotificationRepository
import com.yapp.love.domain.notification.model.Notification
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

interface NotificationJpaRepositoryInterface : JpaRepository<Notification, Long> {
    fun findByUserIdOrderByIdDesc(userId: Long, pageable: Pageable): List<Notification>

    fun findByUserIdAndIdLessThanOrderByIdDesc(userId: Long, id: Long, pageable: Pageable): List<Notification>

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.userId = :userId AND n.isRead = false")
    fun markAllAsReadByUserId(userId: Long)

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :threshold")
    fun deleteByCreatedAtBefore(threshold: LocalDateTime): Int
}

@Repository
class NotificationJpaRepository(
    private val jpaRepository: NotificationJpaRepositoryInterface,
) : NotificationRepository {
    override fun save(notification: Notification): Notification {
        return jpaRepository.save(notification)
    }

    override fun findById(id: Long): Notification? {
        return jpaRepository.findById(id).orElse(null)
    }

    override fun findByUserIdWithCursor(userId: Long, lastId: Long?, size: Int): List<Notification> {
        val pageable = PageRequest.of(0, size + 1)
        return if (lastId == null) {
            jpaRepository.findByUserIdOrderByIdDesc(userId, pageable)
        } else {
            jpaRepository.findByUserIdAndIdLessThanOrderByIdDesc(userId, lastId, pageable)
        }
    }

    override fun markAllAsReadByUserId(userId: Long) {
        jpaRepository.markAllAsReadByUserId(userId)
    }

    override fun deleteCreatedBefore(threshold: LocalDateTime): Int {
        return jpaRepository.deleteByCreatedAtBefore(threshold)
    }
}
