package com.allset.allset.dto

data class ConfirmationStatsResponse(
    val confirmed: Int,
    val pending: Int,
    val notComing: Int,
    val totalGuests: Int
)
