package com.allset.allset.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "promo_code_usages")
data class PromoCodeUsage(
    @Id val id: String? = null,
    val promoCodeId: String,
    val code: String,
    val userId: String,
    val businessName: String,
    val usedAt: Instant = Instant.now()
)
