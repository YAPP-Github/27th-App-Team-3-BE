package com.yapp.love.application.photolog.dto

import com.yapp.love.domain.photolog.model.ReactionType

data class ReactionInfo(
    val photologId: Long,
    val reaction: ReactionType,
)