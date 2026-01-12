package com.yapp.love.globalutils.exception

import org.springframework.http.HttpStatus

interface ErrorCode {
    fun getHttpStatus(): HttpStatus

    fun getCode(): String

    fun getMessage(): String
}
