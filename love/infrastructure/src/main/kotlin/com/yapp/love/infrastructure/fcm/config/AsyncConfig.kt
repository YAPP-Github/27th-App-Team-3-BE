package com.yapp.love.infrastructure.fcm.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@EnableAsync
@Configuration
class AsyncConfig {
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
}