package com.allset.allset.service

import com.allset.allset.config.LocalizationProperties
import com.allset.allset.dto.PricingSummary
import com.allset.allset.model.Template
import com.allset.allset.model.TemplateDefaults
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
    private val pricingService: PricingService,
    private val invitationDefaultsService: InvitationDefaultsService,
    private val s3Service: S3Service
) {

    fun getSupportedLanguages(): List<String> {
        return localizationProperties.supportedLanguages
    }

    fun getTemplateById(id: String): Template? {
        return getTemplates().find { it.id == id }
    }

    fun getTemplates(): List<Template> {
        val appliedPromoCodes = userService.getCurrentUserOrNull()?.appliedPromoCodes ?: emptyList()
        val pricingSummary = pricingService.summarize(appliedPromoCodes)

        return listOf(
            buildTemplate(
                id = "template.rustic.love.story",
                type = TemplateType.Rustic_Love_Story,
                imageUrl = s3Service.getTemplateUrl("rustic_love_story.png"),
                mobileImageUrl = s3Service.getTemplateUrl("rustic_love_story_mobile.png"),
                mainImageMaxCount = 4,
                albumImageMaxCount = 4,
                paletteIds = listOf("warm_linen", "dusty_lilac", "muted_sage", "copper_dusk"),
                pricingSummary = pricingSummary
            ),
            buildTemplate(
                id = "template.modern.romance",
                type = TemplateType.Modern_Romance,
                imageUrl = s3Service.getTemplateUrl("modern_romance.png"),
                mobileImageUrl = s3Service.getTemplateUrl("modern_romance_mobile.png"),
                mainImageMaxCount = 5,
                albumImageMaxCount = 5,
                paletteIds = listOf(
                    "sage_garden", "rosewood", "coastal_mist",
                    "terracotta_sun", "midnight_bloom", "velvet_plum"
                ),
                pricingSummary = pricingSummary
            ),
            buildTemplate(
                id = "template.classic.elegance",
                type = TemplateType.Classic_Elegance,
                imageUrl = s3Service.getTemplateUrl("classic_elegance.png"),
                mobileImageUrl = s3Service.getTemplateUrl("classic_elegance_mobile.png"),
                mainImageMaxCount = 5,
                albumImageMaxCount = 5,
                paletteIds = listOf(
                    "charcoal_noir", "bordeaux", "midnight_navy", "forest_night",
                    "blossom_pink", "aqua_dream", "honey_dew"
                ),
                pricingSummary = pricingSummary
            ),
        )
    }

    private fun buildTemplate(
        id: String,
        type: TemplateType,
        imageUrl: String,
        mobileImageUrl: String,
        mainImageMaxCount: Int,
        albumImageMaxCount: Int,
        paletteIds: List<String>,
        pricingSummary: PricingSummary
    ): Template {
        val defaults = buildDefaults(id)
        return Template(
            id = id,
            type = type,
            name = getLocalizedMessages("$id.name"),
            description = getLocalizedMessages("$id.description"),
            templateImage = imageUrl,
            templateImageMobile = mobileImageUrl,
            mainImageMaxCount = mainImageMaxCount,
            albumImageMaxCount = albumImageMaxCount,
            palettes = colorPaletteService.getByIds(paletteIds),
            pricing = pricingSummary.copy(),
            styleKeyword = getLocalizedMessages("$id.keywords.style"),
            lovedByKeyword = getLocalizedMessages("$id.keywords.lovedBy"),
            createdByKeyword = getLocalizedMessages("$id.keywords.createdBy"),
            paletteKeyword = colorPaletteService.getByIds(paletteIds).first(),
            defaults = defaults
        )
    }

    private fun buildDefaults(templateId: String): TemplateDefaults {
        return TemplateDefaults(
            description = invitationDefaultsService.getDefaultDescription(templateId),
            agendaTitles = invitationDefaultsService.getDefaultAgendaTitles(templateId),
            dressCodeDescription = invitationDefaultsService.getDefaultDressCodeDescription(templateId),
            ourStoryText = invitationDefaultsService.getDefaultOurStoryText(templateId)
        )
    }

    private fun getLocalizedMessages(baseKey: String): Map<String, String> {
        return getSupportedLanguages().associateWith { lang ->
            messageSource.getMessage(baseKey, null, Locale.forLanguageTag(lang))
        }
    }
}
