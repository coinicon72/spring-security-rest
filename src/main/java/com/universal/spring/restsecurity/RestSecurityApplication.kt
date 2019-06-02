package com.universal.spring.restsecurity

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
class RestSecurityApplication

fun main(args: Array<String>) {
	runApplication<RestSecurityApplication>(*args)
}
