package com.yapp.love.infrastructure.oauth.apple.dto

import org.springframework.util.LinkedMultiValueMap

data class AppleIdTokenRequest(
    val grantType: String,
    val code: String,
    val clientId: String,
    val clientSecret: String,
) {
    fun toMultiValueMap(): LinkedMultiValueMap<String, String> {
        return LinkedMultiValueMap<String, String>().apply {
            add("grant_type", grantType)
            add("code", code)
            add("client_id", clientId)
            add("client_secret", clientSecret)
        }
    }
}
