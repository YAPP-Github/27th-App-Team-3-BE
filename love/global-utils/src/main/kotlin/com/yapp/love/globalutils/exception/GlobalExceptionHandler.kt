package com.yapp.love.globalutils.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.net.BindException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = KotlinLogging.logger { }

    @ExceptionHandler(GlobalException::class)
    protected fun handleGlobalException(e: GlobalException): ResponseEntity<ErrorResponse> {
        val errorCode = e.errorCode

        logger.warn { "Parameter binding failed: ${e.message}, ErrorCode: ${errorCode.getCode()}" }

        val error = ErrorResponse.error(errorCode)

        return ResponseEntity(error, errorCode.getHttpStatus())
    }

    @ExceptionHandler(BindException::class)
    protected fun invalidArgumentBindResponse(e: BindException): ResponseEntity<ErrorResponse> {
        val globalErrorCode = GlobalErrorCode.INVALID_INPUT_VALUE

        logger.warn { "Parameter binding failed: ${e.message}" }

        val error = ErrorResponse.error(globalErrorCode)

        return ResponseEntity(error, globalErrorCode.getHttpStatus())
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    protected fun handleHttpRequestMethodNotSupportedException(
        e: HttpRequestMethodNotSupportedException,
    ): ResponseEntity<ErrorResponse> {
        val globalErrorCode = GlobalErrorCode.METHOD_NOT_ALLOWED

        logger.warn { "HTTP method not supported: ${e.message}" }

        val error = ErrorResponse.error(globalErrorCode)

        return ResponseEntity(error, globalErrorCode.getHttpStatus())
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun handleMethodArgumentNotValidException(
        e: MethodArgumentNotValidException,
    ): ResponseEntity<ErrorResponse> {
        val globalErrorCode = GlobalErrorCode.INVALID_INPUT_VALUE

        val errorMsg =
            e.bindingResult.fieldErrors.joinToString(", ") {
                "${it.field}: ${it.defaultMessage}"
            }

        logger.warn { "Validation failed: $errorMsg" }

        val error = ErrorResponse.error(globalErrorCode)

        return ResponseEntity(error, globalErrorCode.getHttpStatus())
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    protected fun handleInvalidJson(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val globalErrorCode = GlobalErrorCode.MALFORMED_JSON

        logger.warn { "Malformed JSON request: ${ex.localizedMessage}" }

        val error = ErrorResponse.error(globalErrorCode)

        return ResponseEntity(error, globalErrorCode.getHttpStatus())
    }

    @ExceptionHandler(Exception::class)
    protected fun handleGeneralException(ex: Exception): ResponseEntity<ErrorResponse> {
        val globalErrorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR

        logger.error(ex) { "Unexpected error occurred: ${ex.message}" }

        val error = ErrorResponse.error(globalErrorCode, "Unexpected error occurred: ${ex.message}")

        return ResponseEntity(error, globalErrorCode.getHttpStatus())
    }
}
