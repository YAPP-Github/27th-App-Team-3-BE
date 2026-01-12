package com.yapp.love.infrastructure.logging

/**
 * 인증 시스템 구현 전 임시로 사용하던 MDC 로깅 필터
 *
 * SecurityMdcLoggingFilter로 교체되었습니다.
 * 이 클래스는 더 이상 사용되지 않습니다.
 * @Component 제거됨 - SecurityMdcLoggingFilter 사용
 */
class SimpleMdcLoggingFilter : BaseMdcLoggingFilter() {
    override fun resolveUserId(): String? = null
}
