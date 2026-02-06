package com.yapp.love.application.storage

interface FileStoragePort {
    fun generatePresignedUploadUrl(key: String): PresignedUrlResult

    fun getPhotologUrl(
        goalId: Long,
        fileName: String,
    ): String
}

data class PresignedUrlResult(
    val uploadUrl: String,
    val fileName: String,
)
