package com.yapp.love.infrastructure.fcm.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

private val logger = KotlinLogging.logger {}

@EnableAsync
@Configuration
class AsyncConfig : AsyncConfigurer {
    // current spec : 0.5 vCPU, 1GB RAM Fargate
    @Bean(name = ["fcmTaskExecutor"])
    fun fcmTaskExecutor(): ThreadPoolTaskExecutor {
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = 2
            maxPoolSize = 4  // 0.5 vCPU에서 I/O bound 작업 기준
            queueCapacity = 100
            setThreadNamePrefix("fcm-async-")
            initialize()
        }
    }

    @Bean(name = ["fcmMarketingPushExecutor"])
    fun fcmMarketingPushExecutor(): ThreadPoolTaskExecutor {
        // 마케팅 발송 특성상 동시에 여러 요청이 들어올 경우가 적음
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = 2 // 500개씩 쪼개서 보내서 스레드를 오래 점유하는 작업
            maxPoolSize = 2
            queueCapacity = 5
            setThreadNamePrefix("marketing-push-")
            initialize()
        }
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler =
        AsyncUncaughtExceptionHandler { ex, method, params ->
            logger.error(ex) { "Async 처리 중 예외 발생: method=${method.name}, params=${params.toList()}" }
        }
}
