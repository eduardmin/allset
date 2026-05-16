package com.allset.allset.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "faqs")
data class FaqItem(
    @Id val id: String? = null,
    val question: Map<String, String>,
    val answer: Map<String, String>,
    val sortOrder: Int = 0
)
