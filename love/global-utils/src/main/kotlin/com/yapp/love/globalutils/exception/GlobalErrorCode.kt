package com.yapp.love.globalutils.exception

import org.springframework.http.HttpStatus

enum class GlobalErrorCode(
    private val httpStatus: HttpStatus,
    private val code: String,
    private val message: String,
) : ErrorCode {
    // ======================================================================
    // 1. 공통 및 서버
    // ======================================================================
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G5000", "서버 내부 오류가 발생했습니다."),

    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "G4050", "허용되지 않은 HTTP 메서드입니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "G4290", "요청 횟수가 너무 많습니다. 잠시 후 다시 시도해주세요."),

    // ======================================================================
    // 2. 입력 값 검증
    // ======================================================================
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "G4000", "요청 본문의 값이 검증 조건을 충족하지 않습니다"),
    MISSING_INPUT_VALUE(HttpStatus.BAD_REQUEST, "G4001", "필수 입력값이 누락되었습니다."),
    MALFORMED_JSON(HttpStatus.BAD_REQUEST, "G4002", "JSON 형식이 올바르지 않습니다."),
    REQUEST_PARAMETER_BIND_FAILED(HttpStatus.BAD_REQUEST, "G4003", "요청값을 필드에 매핑할 수 없습니다."),

    // ======================================================================
    // 3. 인증 및 권한
    // ======================================================================
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "G4010", "인증되지 않은 사용자입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "G4011", "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "G4012", "유효하지 않은 토큰입니다."),

    FORBIDDEN(HttpStatus.FORBIDDEN, "G4030", "접근 권한이 없습니다."),

    // ======================================================================
    // 4. 리소스 및 상태
    // ======================================================================
    NOT_FOUND(HttpStatus.NOT_FOUND, "G4040", "요청한 리소스를 찾을 수 없습니다."),
    ;

    override fun getHttpStatus(): HttpStatus = httpStatus

    override fun getCode(): String = code

    override fun getMessage(): String = message
}
