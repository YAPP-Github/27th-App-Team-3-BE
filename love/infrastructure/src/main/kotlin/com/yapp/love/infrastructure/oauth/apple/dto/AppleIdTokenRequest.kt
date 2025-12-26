package com.yapp.love.infrastructure.oauth.apple.dto

import org.springframework.util.LinkedMultiValueMap

data class AppleIdTokenRequest(
    val grant_type: String,
    val code: String,
    val client_id: String,
    val client_secret: String,
) {
    fun toMultiValueMap(): LinkedMultiValueMap<String, String> {
        return LinkedMultiValueMap<String, String>().apply {
            add("grant_type", grant_type)
            add("code", code)
            add("client_id", client_id)
            add("client_secret", client_secret)
        }
    }
}
