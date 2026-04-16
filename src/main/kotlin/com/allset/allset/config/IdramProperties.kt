package com.allset.allset.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "idram")
data class IdramProperties(
    val recAccount: String = "",
    val secretKey: String = "",
    val successUrl: String = "",
    val failUrl: String = ""
)
