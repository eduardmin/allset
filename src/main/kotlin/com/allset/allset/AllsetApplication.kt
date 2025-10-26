package com.allset.allset

import com.allset.allset.config.LocalizationProperties
import com.allset.allset.config.PricingProperties
import com.allset.allset.config.PromoCodeProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class, HibernateJpaAutoConfiguration::class])
@EnableConfigurationProperties(LocalizationProperties::class, PricingProperties::class, PromoCodeProperties::class)
class AllsetApplication

fun main(args: Array<String>) {
    println("allset")
    runApplication<AllsetApplication>(*args)
}
