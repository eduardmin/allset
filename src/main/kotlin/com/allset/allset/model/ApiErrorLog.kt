package com.allset.allset.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "api_error_logs")
data class ApiErrorLog(
    @Id
    val id: String? = null,
    @Indexed
    val timestamp: Instant = Instant.now(),
    @Indexed
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val method: String,
    @Indexed
    val userId: String? = null,
    val userEmail: String? = null,
    val userSub: String? = null,
    val exceptionType: String? = null,
    val stackTrace: String? = null,
    val queryString: String? = null,
    val clientIp: String? = null,
    val userAgent: String? = null
)
