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
        ),
        buildPalette(
            id = "charcoal_noir",
            colors = listOf("#2A2A2A", "#323232", "#3E433C", "#D9D9D9")
        ),
        buildPalette(
            id = "bordeaux",
            colors = listOf("#240F0F", "#3E1A1A", "#7A3838", "#D9D9D9")
        ),
        buildPalette(
            id = "midnight_navy",
            colors = listOf("#0F1624", "#1A273E", "#2E4A70", "#D9D9D9")
        ),
        buildPalette(
            id = "forest_night",
            colors = listOf("#16332F", "#122B28", "#1E4A42", "#D9D9D9")
        ),
        buildPalette(
            id = "blossom_pink",
            colors = listOf("#A85A72", "#C47A90", "#E8A8BB", "#F5DDE5")
        ),
        buildPalette(
            id = "aqua_dream",
            colors = listOf("#4A8A9A", "#6AACBA", "#9FD0DC", "#D0EEF5")
        ),
        buildPalette(
            id = "honey_dew",
            colors = listOf("#5A9A6A", "#7AB88A", "#A8D8B0", "#D8F0E0")
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
