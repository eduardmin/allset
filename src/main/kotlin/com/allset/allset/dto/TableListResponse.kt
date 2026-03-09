package com.allset.allset.dto

data class TableListResponse(
    val tables: Map<Int, List<String>>,
    val unassignedCount: Int
)
