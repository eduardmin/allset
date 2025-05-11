package com.allset.allset

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class, HibernateJpaAutoConfiguration::class])
class AllsetApplication

fun main(args: Array<String>) {
	println("allset")
	val context = runApplication<AllsetApplication>(*args)
	val mappings = context.getBean(RequestMappingHandlerMapping::class.java)
	mappings.handlerMethods.forEach { (key, value) ->
		println("➡️ $key")
	}
}
