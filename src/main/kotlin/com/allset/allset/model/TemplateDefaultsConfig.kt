package com.allset.allset.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "template_defaults")
data class TemplateDefaultsConfig(
    @Id val id: String? = null,
    @Indexed(unique = true)
    val templateId: String,
    val description: Map<String, String>? = null,
    val agendaTitles: Map<String, Map<String, String>>? = null,
    val dressCodeDescription: Map<String, String>? = null,
    val ourStoryText: Map<String, String>? = null
)
