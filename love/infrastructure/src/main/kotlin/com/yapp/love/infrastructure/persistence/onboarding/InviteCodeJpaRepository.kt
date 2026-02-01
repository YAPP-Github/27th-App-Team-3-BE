package com.yapp.love.infrastructure.persistence.onboarding

import com.yapp.love.domain.onboarding.InviteCodeRepository
import com.yapp.love.domain.onboarding.model.InviteCodes
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface InviteCodeJpaRepository : JpaRepository<InviteCodes, Long> {
    fun existsByCode(code: String): Boolean

    fun findByCreatorId(creatorId: Long): InviteCodes?

    fun findByCode(code: String): InviteCodes?
}

@Repository
class InviteCodeRepositoryImpl(
    private val inviteCodeJpaRepository: InviteCodeJpaRepository
) : InviteCodeRepository {

    override fun isExistByCode(inviteCode: String): Boolean {
        return inviteCodeJpaRepository.existsByCode(inviteCode)
    }

    override fun findByCreatorId(creatorId: Long): InviteCodes? {
        return inviteCodeJpaRepository.findByCreatorId(creatorId)
    }

    override fun findByCode(code: String): InviteCodes? {
        return inviteCodeJpaRepository.findByCode(code)
    }

    override fun save(inviteCode: InviteCodes): InviteCodes {
        return inviteCodeJpaRepository.save(inviteCode)
    }
}
