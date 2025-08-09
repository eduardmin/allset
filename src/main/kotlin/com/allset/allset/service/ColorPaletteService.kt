package com.allset.allset.service

import com.allset.allset.config.LocalizationProperties
import com.allset.allset.model.ColorPalette
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class ColorPaletteService(
    private val messageSource: MessageSource,
    private val localizationProperties: LocalizationProperties
) {
    fun getSupportedLanguages(): List<String> {
        return localizationProperties.supportedLanguages
    }
    private val palettes: Map<String, ColorPalette> = listOf(
        buildPalette(
            id = "romantic_rose",
            colors = listOf("#FADADD", "#F7B2B7", "#E75480", "#9B2242")
        ),
        buildPalette(
            id = "classic_elegance",
            colors = listOf("#F4F4F4", "#D1D5DB", "#4B5563", "#111827")
        ),
        buildPalette(
            id = "garden_party",
            colors = listOf("#A7F3D0", "#34D399", "#059669", "#065F46")
        ),
        buildPalette(
            id = "golden_sunset",
            colors = listOf("#FEF3C7", "#FCD34D", "#F59E0B", "#B45309")
        ),
        buildPalette(
            id = "ocean_breeze",
            colors = listOf("#DBEAFE", "#93C5FD", "#3B82F6", "#1E3A8A")
        )
    ).associateBy { it.id }

    fun getAll(): List<ColorPalette> = palettes.values.toList()

    fun getById(id: String): ColorPalette? = palettes[id]

    fun getByIds(ids: List<String>): List<ColorPalette> = ids.mapNotNull { palettes[it] }



    private fun buildPalette(id: String, colors: List<String>): ColorPalette =
        ColorPalette(
            id = id,
            name = getLocalizedMessages("palette.$id.name"),
            description = getLocalizedMessages("palette.$id.description"),
            colors = colors
        )

    private fun getLocalizedMessages(key: String): Map<String, String> =
        getSupportedLanguages().associateWith { lang ->
            messageSource.getMessage(key, null, Locale.forLanguageTag(lang))
        }
}
