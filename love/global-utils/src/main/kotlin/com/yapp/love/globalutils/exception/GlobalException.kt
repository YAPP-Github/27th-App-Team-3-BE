package com.yapp.love.globalutils.exception

import org.springframework.web.server.ResponseStatusException

open class GlobalException(
    val errorCode: ErrorCode,
    private val customMessage: String? = null,
) : ResponseStatusException(
        errorCode.getHttpStatus(),
        customMessage ?: errorCode.getMessage(),
    ) {
    fun getCustomMessage(): String {
        return customMessage ?: errorCode.getMessage()
    }
}
