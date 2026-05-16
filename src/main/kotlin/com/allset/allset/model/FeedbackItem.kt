package com.allset.allset.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "feedbacks")
data class FeedbackItem(
    @Id val id: String? = null,
    val name: String,
    val image: String,
    val count: Int,
    val rating: Int,
    val text: Map<String, String>
)
