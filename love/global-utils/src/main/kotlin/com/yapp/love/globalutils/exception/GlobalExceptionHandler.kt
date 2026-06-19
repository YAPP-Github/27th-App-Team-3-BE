package com.yapp.love.globalutils.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
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

        logger.info { "GlobalException: ${errorCode.getCode()} - ${e.message}" }

        val error = ErrorResponse.from(errorCode, e.getCustomMessage())

        return ResponseEntity(error, errorCode.getHttpStatus())
    }

    @ExceptionHandler(BindException::class)
    protected fun invalidArgumentBindResponse(e: BindException): ResponseEntity<ErrorResponse> {
        val globalErrorCode = GlobalErrorCode.INVALID_INPUT_VALUE

        logger.info { "Parameter binding failed: ${e.message}" }

        val error = ErrorResponse.from(globalErrorCode)

        return ResponseEntity(error, globalErrorCode.getHttpStatus())
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    protected fun handleHttpRequestMethodNotSupportedException(
        e: HttpRequestMethodNotSupportedException,
    ): ResponseEntity<ErrorResponse> {
        val globalErrorCode = GlobalErrorCode.METHOD_NOT_ALLOWED

        logger.info { "HTTP method not supported: ${e.message}" }

        val error = ErrorResponse.from(globalErrorCode)

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

        logger.info { "Validation failed: $errorMsg" }

        val error = ErrorResponse.from(globalErrorCode)

        return ResponseEntity(error, globalErrorCode.getHttpStatus())
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    protected fun handleMethodArgumentTypeMismatchException(
        e: MethodArgumentTypeMismatchException,
    ): ResponseEntity<ErrorResponse> {
        val globalErrorCode = GlobalErrorCode.INVALID_INPUT_VALUE

        logger.info { "Type mismatch for parameter '${e.name}': ${e.value}" }

        val error = ErrorResponse.from(globalErrorCode, "입력값이 올바르지 않습니다.")

        return ResponseEntity(error, globalErrorCode.getHttpStatus())
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    protected fun handleInvalidJson(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val globalErrorCode = GlobalErrorCode.MALFORMED_JSON

        logger.info { "Malformed JSON request: ${ex.message}" }

        val error = ErrorResponse.from(globalErrorCode)

        return ResponseEntity(error, globalErrorCode.getHttpStatus())
    }

    @ExceptionHandler(NoResourceFoundException::class)
    protected fun handleNoResourceFound(ex: NoResourceFoundException): ResponseEntity<ErrorResponse> {
        val globalErrorCode = GlobalErrorCode.NOT_FOUND

        logger.info { "Resource not found: ${ex.resourcePath}" }

        val error = ErrorResponse.from(globalErrorCode)

        return ResponseEntity(error, globalErrorCode.getHttpStatus())
    }

    @ExceptionHandler(AuthenticationException::class)
    protected fun handleAuthenticationException(ex: AuthenticationException): ResponseEntity<ErrorResponse> {
        val globalErrorCode = GlobalErrorCode.UNAUTHORIZED

        logger.info { "Authentication failed: ${ex.message}" }

        val error = ErrorResponse.from(globalErrorCode)

        return ResponseEntity(error, globalErrorCode.getHttpStatus())
    }

    @ExceptionHandler(AccessDeniedException::class)
    protected fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        val globalErrorCode = GlobalErrorCode.FORBIDDEN

        logger.info { "Access denied: ${ex.message}" }

        val error = ErrorResponse.from(globalErrorCode)

        return ResponseEntity(error, globalErrorCode.getHttpStatus())
    }

    @ExceptionHandler(IllegalArgumentException::class, IllegalStateException::class)
    protected fun handleIllegalException(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        val globalErrorCode = GlobalErrorCode.INVALID_INPUT_VALUE

        logger.info { "Domain validation failed: ${ex.message}" }

        val error =
            ErrorResponse(
                status = globalErrorCode.getHttpStatus().value(),
                code = globalErrorCode.getCode(),
                message = ex.message ?: globalErrorCode.getMessage(),
            )

        return ResponseEntity(error, globalErrorCode.getHttpStatus())
    }

    @ExceptionHandler(Exception::class)
    protected fun handleGeneralException(ex: Exception): ResponseEntity<ErrorResponse> {
        val globalErrorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR

        logger.info { "Unexpected error occurred: ${ex.message}" }

        val error = ErrorResponse.from(globalErrorCode)

        return ResponseEntity(error, globalErrorCode.getHttpStatus())
    }
}
