package com.allset.allset.service

import com.allset.allset.config.LocalizationProperties
import com.allset.allset.model.Template
import com.allset.allset.model.TemplateType
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class TemplateService(private val messageSource: MessageSource,
                      private val localizationProperties: LocalizationProperties,
                      private val colorPaletteService: ColorPaletteService
) {

    fun getSupportedLanguages(): List<String> {
        return localizationProperties.supportedLanguages
    }

    fun getTemplates(): List<Template> {
        return listOf(
            buildTemplate(
                id = "template.romantic",
                type = TemplateType.ROMANTIC,
                imageUrl = "http://localhost:8080/templates/romantic.png",
                mainImageMaxCount = 5,
                albumImageMaxCount = 5,
                paletteIds = listOf("romantic_rose", "classic_elegance")
            ),
            buildTemplate(
                id = "template.armenian-chic",
                type = TemplateType.ARMENIAN_CHIC,
                imageUrl = "http://localhost:8080/templates/armenian.png",
                mainImageMaxCount = 5,
                albumImageMaxCount = 5,
                paletteIds = listOf("garden_party", "golden_sunset")
            ),
            buildTemplate(
                id = "template.elegant-classy",
                type = TemplateType.ELEGANT_CLASSY,
                imageUrl = "http://localhost:8080/templates/classic.png",
                mainImageMaxCount = 4,
                albumImageMaxCount = 4,
                paletteIds = listOf("classic_elegance", "ocean_breeze")
            )
        )
    }

    private fun buildTemplate(
        id: String,
        type: TemplateType,
        imageUrl: String,
        mainImageMaxCount: Int,
        albumImageMaxCount: Int,
        paletteIds: List<String>
    ): Template {
        return Template(
            id = id,
            type = type,
            name = getLocalizedMessages("$id.name"),
            description = getLocalizedMessages("$id.description"),
            templateImage = imageUrl,
            mainImageMaxCount = mainImageMaxCount,
            albumImageMaxCount = albumImageMaxCount,
            palettes = colorPaletteService.getByIds(paletteIds)
        )
    }

    private fun getLocalizedMessages(baseKey: String): Map<String, String> {
        return getSupportedLanguages().associateWith { lang ->
            messageSource.getMessage(baseKey, null, Locale.forLanguageTag(lang))
        }
    }
}
