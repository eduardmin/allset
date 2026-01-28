package com.allset.allset.service

import com.allset.allset.config.LocalizationProperties
import com.allset.allset.dto.PricingSummary
import com.allset.allset.model.Template
import com.allset.allset.model.TemplateType
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class TemplateService(
    private val messageSource: MessageSource,
    private val localizationProperties: LocalizationProperties,
    private val colorPaletteService: ColorPaletteService,
    private val userService: UserService,
    private val pricingService: PricingService
) {

    fun getSupportedLanguages(): List<String> {
        return localizationProperties.supportedLanguages
    }

    fun getTemplates(): List<Template> {
        val appliedPromoCodes = userService.getCurrentUserOrNull()?.appliedPromoCodes ?: emptyList()
        val pricingSummary = pricingService.summarize(appliedPromoCodes)

        return listOf(
            buildTemplate(
                id = "template.classic.elegance",
                type = TemplateType.Classic_Elegance,
                imageUrl = "/templates/classic_elegance.png",
                mainImageMaxCount = 5,
                albumImageMaxCount = 5,
                paletteIds = listOf("romantic_rose", "classic_elegance"),
                pricingSummary = pricingSummary
            ),
            buildTemplate(
                id = "template.modern.romance",
                type = TemplateType.Modern_Romance,
                imageUrl = "/templates/modern_romance.png",
                mainImageMaxCount = 5,
                albumImageMaxCount = 5,
                paletteIds = listOf("garden_party", "golden_sunset"),
                pricingSummary = pricingSummary
            ),
            buildTemplate(
                id = "template.rustic.love.story",
                type = TemplateType.Rustic_Love_Story,
                imageUrl = "/templates/rustic_love_story.png",
                mainImageMaxCount = 4,
                albumImageMaxCount = 4,
                paletteIds = listOf("classic_elegance", "ocean_breeze"),
                pricingSummary = pricingSummary
            )
        )
    }

    private fun buildTemplate(
        id: String,
        type: TemplateType,
        imageUrl: String,
        mainImageMaxCount: Int,
        albumImageMaxCount: Int,
        paletteIds: List<String>,
        pricingSummary: PricingSummary
    ): Template {
        return Template(
            id = id,
            type = type,
            name = getLocalizedMessages("$id.name"),
            description = getLocalizedMessages("$id.description"),
            templateImage = imageUrl,
            mainImageMaxCount = mainImageMaxCount,
            albumImageMaxCount = albumImageMaxCount,
            palettes = colorPaletteService.getByIds(paletteIds),
            pricing = pricingSummary.copy(),
            styleKeyword = getLocalizedMessages("$id.keywords.style"),
            lovedByKeyword = getLocalizedMessages("$id.keywords.lovedBy"),
            createdByKeyword = getLocalizedMessages("$id.keywords.createdBy"),
            paletteKeyword = colorPaletteService.getByIds(paletteIds).first()
            )
    }

    private fun getLocalizedMessages(baseKey: String): Map<String, String> {
        return getSupportedLanguages().associateWith { lang ->
            messageSource.getMessage(baseKey, null, Locale.forLanguageTag(lang))
        }
    }
}
