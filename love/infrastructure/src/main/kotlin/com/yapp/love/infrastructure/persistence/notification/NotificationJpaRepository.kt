package com.yapp.love.infrastructure.persistence.notification

import com.yapp.love.domain.notification.NotificationRepository
import com.yapp.love.domain.notification.model.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

interface NotificationJpaRepositoryInterface : JpaRepository<Notification, Long> {
    fun findByUserId(userId: Long): List<Notification>

    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<Notification>

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    fun countUnreadByUserId(userId: Long): Long

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.userId = :userId AND n.isRead = false")
    fun markAllAsReadByUserId(userId: Long): Int

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

    override fun findByUserId(userId: Long): List<Notification> {
        return jpaRepository.findByUserId(userId)
    }

    override fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<Notification> {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId)
    }

    override fun countUnreadByUserId(userId: Long): Long {
        return jpaRepository.countUnreadByUserId(userId)
    }

    override fun markAllAsReadByUserId(userId: Long): Int {
        return jpaRepository.markAllAsReadByUserId(userId)
    }

    override fun deleteCreatedBefore(threshold: LocalDateTime): Int {
        return jpaRepository.deleteByCreatedAtBefore(threshold)
    }
}
