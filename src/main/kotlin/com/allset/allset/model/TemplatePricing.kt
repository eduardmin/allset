package com.allset.allset.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal

@Document(collection = "template_pricing")
data class TemplatePricing(
    @Id val id: String? = null,
    @Indexed(unique = true)
    val templateId: String,
    val basePrice: BigDecimal
)
