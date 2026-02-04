package com.allset.allset.dto

data class ConfirmationFilterDTO(
    val id: String,
    val label: Map<String, String>
)

data class ConfirmationFiltersResponse(
    val filters: List<ConfirmationFilterDTO>
)
