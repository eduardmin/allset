package com.allset.allset.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app.localization")
class LocalizationProperties {
    var supportedLanguages: List<String> = listOf("en")
}