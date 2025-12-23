package com.yapp.love.globalutils.exception

import org.springframework.web.server.ResponseStatusException

open class GlobalException(
    val errorCode: ErrorCode,
    message: String? = null,
) : ResponseStatusException(
        errorCode.getHttpStatus(),
        message ?: errorCode.getMessage(),
    )
