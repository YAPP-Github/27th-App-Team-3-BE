package com.yapp.love.infrastructure.logging

import org.slf4j.MDC
import org.springframework.core.task.TaskDecorator

/**
 * 비동기 작업에서 MDC 컨텍스트를 전파하는 TaskDecorator
 *
 * @Async 메서드에서도 traceId, userId 등이 유지됩니다.
 *
 * 사용법:
 * ```
 * @Configuration
 * class AsyncConfig {
 *     @Bean
 *     fun taskExecutor(): TaskExecutor {
 *         val executor = ThreadPoolTaskExecutor()
 *         executor.setTaskDecorator(MdcTaskDecorator())
 *         executor.initialize()
 *         return executor
 *     }
 * }
 * ```
 */
class MdcTaskDecorator : TaskDecorator {
    override fun decorate(runnable: Runnable): Runnable {
        val contextMap = MDC.getCopyOfContextMap()

        return Runnable {
            val previous = MDC.getCopyOfContextMap()

            try {
                contextMap?.let(MDC::setContextMap) ?: MDC.clear()
                runnable.run()
            } finally {
                previous?.let(MDC::setContextMap) ?: MDC.clear()
            }
        }
    }
}
