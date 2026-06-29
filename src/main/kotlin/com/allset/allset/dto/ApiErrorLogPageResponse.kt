package com.allset.allset.dto

import com.allset.allset.model.ApiErrorLog

data class ApiErrorLogPageResponse(
    val items: List<ApiErrorLog>,
    val total: Long,
    val page: Int,
    val size: Int
)
