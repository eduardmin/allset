package com.allset.allset.model

data class HomeResponse(
    val faq: List<FaqItem>,
    val feedbacks: List<FeedbackItem>,
    val promoBanner: PromoBanner
)

data class FaqItem(
    val question: Map<String, String>,
    val answer: Map<String, String>
)

data class FeedbackItem(
    val name: String,
    val image: String,
    val count: Int,
    val rating: Int,
    val text: Map<String, String>
)

data class PromoBanner(
    val title: Map<String, String>,
    val description: Map<String, String>
)
