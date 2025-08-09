package com.allset.allset.model

data class ColorPalette(
    val id: String,
    val name: Map<String, String>,
    val description: Map<String, String>,
    val colors: List<String>
)