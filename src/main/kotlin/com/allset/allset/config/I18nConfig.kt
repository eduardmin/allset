package com.allset.allset.config

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource

@Configuration
class I18nConfig {
    @Bean
    fun messageSource(): MessageSource =
        ResourceBundleMessageSource().apply {
            setBasenames("messages")          // looks for messages_en.properties, etc.
            setDefaultEncoding("UTF-8")
            setFallbackToSystemLocale(false)
            setUseCodeAsDefaultMessage(true)  // returns the key instead of throwing
        }
}
