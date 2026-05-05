package com.allset.allset.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "dress_code_palettes")
data class DressCodePalette(
    @Id val id: String? = null,
    val name: Map<String, String>,
    val description: Map<String, String> = emptyMap(),
    val colors: List<String>
)
