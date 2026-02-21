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
    @Bean(name = ["fcmTaskExecutor"])
    fun fcmTaskExecutor(): ThreadPoolTaskExecutor {
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = 2
            maxPoolSize = 10
            queueCapacity = 500
            setThreadNamePrefix("fcm-async-")
            initialize()
        }
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler =
        AsyncUncaughtExceptionHandler { ex, method, params ->
            logger.error(ex) { "Async 처리 중 예외 발생: method=${method.name}, params=${params.toList()}" }
        }
}