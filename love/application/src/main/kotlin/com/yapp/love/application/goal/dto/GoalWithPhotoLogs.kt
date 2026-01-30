package com.yapp.love.application.goal.dto

import com.yapp.love.domain.goal.model.Goal
import com.yapp.love.domain.photolog.model.Photolog

data class GoalWithPhotoLogs(
    val goal: Goal,
    val myPhotolog: Photolog?,
    val partnerPhotolog: Photolog?
)
