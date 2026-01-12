package com.yapp.love.infrastructure.logging

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * 인증 시스템 구현 전 임시로 사용하는 MDC 로깅 필터
 *
 * 사용자 ID를 추출하지 않고 모든 요청을 GUEST로 처리합니다.
 *
 * TODO: social-login 브랜치 머지 후 SecurityMdcLoggingFilter로 교체
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class SimpleMdcLoggingFilter : BaseMdcLoggingFilter() {
    override fun resolveUserId(): String? = null
}
