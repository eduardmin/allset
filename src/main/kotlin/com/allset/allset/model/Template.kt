package com.allset.allset.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class Template(
    val id: String,
    val type: TemplateType,
    val name: String,
    val templateImage: String,
    val templateDescription: String,
    val invitationModel: InvitationModel,
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
    ROMANTIC,
    ARMENIAN_CHIC,
    ELEGANT_CLASSY;

    @JsonCreator
    fun fromString(key: String): TemplateType =
        entries.firstOrNull { it.name.equals(key.replace(" ", "_"), ignoreCase = true) }
            ?: throw IllegalArgumentException("Unknown template type: $key")

    @JsonValue
    fun toValue(): String = name.lowercase().replace("_", " ")
}
