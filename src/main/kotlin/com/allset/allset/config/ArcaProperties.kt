package com.allset.allset.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "arca")
data class ArcaProperties(
    val userName: String = "",
    val password: String = "",
    val baseUrl: String = "https://ipaytest.arca.am:8445/payment/rest",
    val returnUrl: String = "",
    val currency: String = "051",
    // Frontend host used to build the post-payment redirect to the /{lang}/build/confirm page
    val confirmBaseUrl: String = "https://development.d1uukuuqwvhgzl.amplifyapp.com"
)
