package com.allset.allset.model

import com.allset.allset.dto.PricingSummary
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class Template(
    val id: String,
    val type: TemplateType,
    val name: Map<String, String>,
    val description: Map<String, String>,
    val templateImage: String,
    val mainImageMaxCount: Int,
    val albumImageMaxCount: Int,
    val palettes: List<ColorPalette>,
    val pricing: PricingSummary,
    val styleKeyword: Map<String, String>,
    val lovedByKeyword: Map<String, String>,
    val createdByKeyword: Map<String, String>,
    val paletteKeyword: ColorPalette
)

data class InvitationModel(
    val mail: Boolean = true,
    val date: Boolean = true,
    val title: Boolean = true,
    val description: Boolean = true,
    val mainImage: Boolean = true,
    val closingText: Boolean = true,
    val timeline: Boolean = true,
    val dressCode: Boolean = true,
    val confirmation: Boolean = true,
    val album: Boolean = true,
    val connectWithUs: Boolean = true
)

enum class TemplateType {
    Classic_Elegance,
    Modern_Romance,
    Rustic_Love_Story;

    @JsonCreator
    fun fromString(key: String): TemplateType =
        entries.firstOrNull { it.name.equals(key.replace(" ", "_"), ignoreCase = true) }
            ?: throw IllegalArgumentException("Unknown template type: $key")

    @JsonValue
    fun toValue(): String = name.lowercase().replace("_", " ")
}
