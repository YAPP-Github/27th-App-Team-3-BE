package com.yapp.love.infrastructure.config

import org.springframework.boot.web.client.RestClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import java.time.Duration

@Configuration
class RestClientConfig {

    @Bean
    fun restClientCustomizer(): RestClientCustomizer {
        return RestClientCustomizer { builder ->
            val factory = SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(CONNECT_TIMEOUT)
                setReadTimeout(READ_TIMEOUT)
            }
            builder.requestFactory(factory)
        }
    }

    companion object {
        private val CONNECT_TIMEOUT = Duration.ofSeconds(5)
        private val READ_TIMEOUT = Duration.ofSeconds(10)
    }
}