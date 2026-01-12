package com.yapp.love.globalutils.exception

import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder("status", "code", "message")
data class ErrorResponse(
    val status: Int,
    val code: String,
    val message: String,
) {
    companion object {
        // [실패] ErrorCode 인터페이스를 사용하는 경우
        fun from(errorCode: ErrorCode): ErrorResponse {
            return ErrorResponse(
                status = errorCode.getHttpStatus().value(),
                code = errorCode.getCode(),
                message = errorCode.getMessage(),
            )
        }

        // [실패] 메시지를 직접 덮어쓰는 경우
        fun from(
            errorCode: ErrorCode,
            message: String,
        ): ErrorResponse {
            return ErrorResponse(
                status = errorCode.getHttpStatus().value(),
                code = errorCode.getCode(),
                message = message,
            )
        }
    }
}
