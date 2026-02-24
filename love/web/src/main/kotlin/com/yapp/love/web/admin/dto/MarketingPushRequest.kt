package com.yapp.love.web.admin.dto

data class MarketingPushRequest(
    val title: String,
    val body: String,
    val deepLink: String? = null,
    val dryRun: Boolean = false,
)
