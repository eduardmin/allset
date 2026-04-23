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
        ),
        buildPalette(
            id = "sage_garden",
            colors = listOf("#3E433C", "#6F786C", "#A3AAA1", "#D3C6B1")
        ),
        buildPalette(
            id = "rosewood",
            colors = listOf("#5C474E", "#947575", "#C4A4A4", "#E8D5D5")
        ),
        buildPalette(
            id = "coastal_mist",
            colors = listOf("#2A5C5E", "#6A8D8E", "#7EA7A8", "#B0D0D1")
        ),
        buildPalette(
            id = "terracotta_sun",
            colors = listOf("#5C2E20", "#8B4A3A", "#C4897A", "#D4B8A8")
        ),
        buildPalette(
            id = "midnight_bloom",
            colors = listOf("#2C2E52", "#4A4E7A", "#8A8EB5", "#C8C5DC")
        ),
        buildPalette(
            id = "velvet_plum",
            colors = listOf("#52334A", "#7A5468", "#B08898", "#D4BECB")
        ),
        buildPalette(
            id = "warm_linen",
            colors = listOf("#3D3B33", "#D9D0B0", "#E4E8DE", "#F5F5F5")
        ),
        buildPalette(
            id = "dusty_lilac",
            colors = listOf("#332E48", "#7060A0", "#C9B8E0", "#E0DCEA")
        ),
        buildPalette(
            id = "muted_sage",
            colors = listOf("#2E3D2E", "#6D9060", "#9AB88A", "#D8E0D2")
        ),
        buildPalette(
            id = "copper_dusk",
            colors = listOf("#3D2010", "#A05A38", "#C87E5A", "#EDE0D4")
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
