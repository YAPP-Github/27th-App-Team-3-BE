package com.yapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication(scanBasePackages = ["com.yapp"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
