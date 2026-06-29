package com.allset.allset.service

import com.allset.allset.config.LocalizationProperties
import com.allset.allset.config.PricingProperties
import com.allset.allset.model.AppliedPromoCode
import com.allset.allset.model.Template
import com.allset.allset.model.TemplateDefaults
import com.allset.allset.model.TemplateType
import com.allset.allset.model.TemplatePricing
import com.allset.allset.repository.TemplatePricingRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class TemplateService(
    private val messageSource: MessageSource,
    private val localizationProperties: LocalizationProperties,
    private val colorPaletteService: ColorPaletteService,
    private val userService: UserService,
    private val pricingService: PricingService,
    private val invitationDefaultsService: InvitationDefaultsService,
    private val s3Service: S3Service,
    private val templatePricingRepository: TemplatePricingRepository,
    private val pricingProperties: PricingProperties
) {

    private val logger = LoggerFactory.getLogger(TemplateService::class.java)

    private val defaultPrices = mapOf(
        "template.rustic.love.story" to BigDecimal("18000"),
        "template.modern.romance" to BigDecimal("15000"),
        "template.classic.elegance" to BigDecimal("12000")
    )

    @PostConstruct
    fun seedDefaultPricing() {
        // Never let a transient MongoDB issue at startup crash the application context
        // (which would fail the deployment and roll back). Log and continue instead.
        runCatching {
            defaultPrices.forEach { (templateId, price) ->
                if (templatePricingRepository.findByTemplateId(templateId) == null) {
                    templatePricingRepository.save(TemplatePricing(templateId = templateId, basePrice = price))
                    logger.info("Seeded default pricing for $templateId: $price")
                }
            }
        }.onFailure { ex ->
            logger.error("Failed to seed default template pricing at startup; continuing without it", ex)
        }
    }

    fun getSupportedLanguages(): List<String> {
        return localizationProperties.supportedLanguages
    }

    fun getTemplateById(id: String): Template? {
        return getTemplates().find { it.id == id }
    }

    // Lightweight templateId -> type lookup that avoids rebuilding the full template list
    // (pricing, S3 URLs, palettes, etc.). Kept in sync with the ids declared in getTemplates().
    fun getTemplateType(templateId: String): TemplateType? = when (templateId) {
        "template.rustic.love.story" -> TemplateType.Rustic_Love_Story
        "template.modern.romance" -> TemplateType.Modern_Romance
        "template.classic.elegance" -> TemplateType.Classic_Elegance
        else -> null
    }

    fun getBasePriceForTemplate(templateId: String): BigDecimal {
        return templatePricingRepository.findByTemplateId(templateId)?.basePrice
            ?: pricingProperties.basePrice
    }

    fun getTemplates(): List<Template> {
        val appliedPromoCodes = userService.getCurrentUserOrNull()?.appliedPromoCodes ?: emptyList()

        return listOf(
            buildTemplate(
                id = "template.rustic.love.story",
                type = TemplateType.Rustic_Love_Story,
                imageUrl = s3Service.getTemplateUrl("rustic_love_story.png"),
                mobileImageUrl = s3Service.getTemplateUrl("rustic_love_story_mobile.png"),
                mainImageMaxCount = 5,
                albumImageMaxCount = 3,
                paletteIds = listOf("warm_linen", "dusty_lilac", "muted_sage", "copper_dusk"),
                appliedPromoCodes = appliedPromoCodes
            ),
            buildTemplate(
                id = "template.modern.romance",
                type = TemplateType.Modern_Romance,
                imageUrl = s3Service.getTemplateUrl("modern_romance.png"),
                mobileImageUrl = s3Service.getTemplateUrl("modern_romance_mobile.png"),
                mainImageMaxCount = 6,
                albumImageMaxCount = 2,
                paletteIds = listOf(
                    "sage_garden", "rosewood", "coastal_mist",
                    "terracotta_sun", "midnight_bloom", "velvet_plum"
                ),
                appliedPromoCodes = appliedPromoCodes
            ),
            buildTemplate(
                id = "template.classic.elegance",
                type = TemplateType.Classic_Elegance,
                imageUrl = s3Service.getTemplateUrl("classic_elegance.png"),
                mobileImageUrl = s3Service.getTemplateUrl("classic_elegance_mobile.png"),
                mainImageMaxCount = 1,
                albumImageMaxCount = 4,
                paletteIds = listOf(
                    "charcoal_noir", "bordeaux", "midnight_navy", "forest_night",
                    "blossom_pink", "aqua_dream", "honey_dew"
                ),
                appliedPromoCodes = appliedPromoCodes
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
        appliedPromoCodes: List<AppliedPromoCode>
    ): Template {
        val templateBasePrice = getBasePriceForTemplate(id)
        val pricingSummary = pricingService.summarize(appliedPromoCodes, templateBasePrice)
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
            pricing = pricingSummary,
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
