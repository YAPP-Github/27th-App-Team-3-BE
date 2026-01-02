package com.yapp.love.globalutils.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.net.BindException

/**
 * 전역 예외 처리 핸들러
 *
 * 요청 정보(traceId, userId, clientIp, requestUri)는 MdcLoggingFilter에서
 * MDC에 설정되어 logback 패턴을 통해 자동으로 로그에 포함됩니다.
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = KotlinLogging.logger { }

    @ExceptionHandler(GlobalException::class)
    protected fun handleGlobalException(e: GlobalException): ResponseEntity<ErrorResponse> {
        val errorCode = e.errorCode

        logger.warn(e) { "GlobalException: ${errorCode.getCode()}" }

        val error = ErrorResponse.error(errorCode)

        return ResponseEntity(error, errorCode.getHttpStatus())
    }

    @ExceptionHandler(BindException::class)
    protected fun invalidArgumentBindResponse(e: BindException): ResponseEntity<ErrorResponse> {
        val globalErrorCode = GlobalErrorCode.INVALID_INPUT_VALUE

        logger.warn(e) { "Parameter binding failed" }

        val error = ErrorResponse.error(globalErrorCode)

        return ResponseEntity(error, globalErrorCode.getHttpStatus())
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    protected fun handleHttpRequestMethodNotSupportedException(
        e: HttpRequestMethodNotSupportedException,
    ): ResponseEntity<ErrorResponse> {
        val globalErrorCode = GlobalErrorCode.METHOD_NOT_ALLOWED

        logger.warn(e) { "HTTP method not supported" }

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

        logger.warn(e) { "Validation failed: $errorMsg" }

        val error = ErrorResponse.error(globalErrorCode)

        return ResponseEntity(error, globalErrorCode.getHttpStatus())
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    protected fun handleInvalidJson(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val globalErrorCode = GlobalErrorCode.MALFORMED_JSON

        logger.warn(ex) { "Malformed JSON request" }

        val error = ErrorResponse.error(globalErrorCode)

        return ResponseEntity(error, globalErrorCode.getHttpStatus())
    }

    @ExceptionHandler(NoResourceFoundException::class)
    protected fun handleNoResourceFound(ex: NoResourceFoundException): ResponseEntity<ErrorResponse> {
        val globalErrorCode = GlobalErrorCode.NOT_FOUND

        logger.warn(ex) { "Resource not found: ${ex.resourcePath}" }

        val error = ErrorResponse.error(globalErrorCode)

        return ResponseEntity(error, globalErrorCode.getHttpStatus())
    }

    @ExceptionHandler(Exception::class)
    protected fun handleGeneralException(ex: Exception): ResponseEntity<ErrorResponse> {
        val globalErrorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR

        // Log4j2 Sentry Appender가 ERROR 로그를 자동으로 Sentry에 전송
        logger.error(ex) { "Unexpected error occurred" }

        val error = ErrorResponse.error(globalErrorCode)

        return ResponseEntity(error, globalErrorCode.getHttpStatus())
    }
}
