package com.yapp.love.infrastructure.oauth.google.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GoogleTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("id_token")
    val idToken: String,
    @JsonProperty("refresh_token")
    val refreshToken: String?,
    @JsonProperty("expires_in")
    val expiresIn: Int,
    @JsonProperty("token_type")
    val tokenType: String,
    val scope: String?,
)
