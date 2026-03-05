package com.allset.allset.service

import com.allset.allset.config.LocalizationProperties
import com.allset.allset.model.*
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class HomeService(
    private val messageSource: MessageSource,
    private val localizationProperties: LocalizationProperties,
    private val s3Service: S3Service
) {

    companion object {
        private const val FAQ_COUNT = 5
        private const val FEEDBACK_COUNT = 3
    }

    fun getHomeData(): HomeResponse {
        return HomeResponse(
            faq = buildFaq(),
            feedbacks = buildFeedbacks(),
            promoBanner = buildPromoBanner()
        )
    }

    private fun buildFaq(): List<FaqItem> {
        return (1..FAQ_COUNT).map { index ->
            FaqItem(
                question = getLocalizedMessages("faq.$index.question"),
                answer = getLocalizedMessages("faq.$index.answer")
            )
        }
    }

    private fun buildFeedbacks(): List<FeedbackItem> {
        return (1..FEEDBACK_COUNT).map { index ->
            FeedbackItem(
                name = getMessage("feedback.$index.name", "en"),
                image = s3Service.getTemplateUrl(getMessage("feedback.$index.image", "en")),
                count = getMessage("feedback.$index.count", "en").toIntOrNull() ?: 0,
                rating = getMessage("feedback.$index.rating", "en").toIntOrNull() ?: 5,
                text = getLocalizedMessages("feedback.$index.text")
            )
        }
    }

    private fun buildPromoBanner(): PromoBanner {
        return PromoBanner(
            title = getLocalizedMessages("promo.banner.title"),
            description = getLocalizedMessages("promo.banner.description")
        )
    }

    private fun getLocalizedMessages(baseKey: String): Map<String, String> {
        return localizationProperties.supportedLanguages.associateWith { lang ->
            messageSource.getMessage(baseKey, null, Locale.forLanguageTag(lang))
        }
    }

    private fun getMessage(key: String, lang: String): String {
        return messageSource.getMessage(key, null, Locale.forLanguageTag(lang))
    }
}
