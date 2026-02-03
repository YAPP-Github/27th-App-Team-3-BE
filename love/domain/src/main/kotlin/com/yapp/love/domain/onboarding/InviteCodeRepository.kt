package com.yapp.love.domain.onboarding

import com.yapp.love.domain.onboarding.model.InviteCodes

interface InviteCodeRepository {
    fun isExistByCode(inviteCode: String): Boolean

    fun findByCreatorId(creatorId: Long): InviteCodes?

    fun findByCode(code: String): InviteCodes?

    fun findUsedByUserId(userId: Long): InviteCodes?

    fun save(inviteCode: InviteCodes): InviteCodes
}
