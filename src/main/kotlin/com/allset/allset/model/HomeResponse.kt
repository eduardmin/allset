package com.allset.allset.model

data class HomeResponse(
    val faq: List<FaqItem>,
    val feedbacks: List<FeedbackItem>,
    val promoBanner: PromoBanner
)

data class PromoBanner(
    val title: Map<String, String>,
    val description: Map<String, String>
)
