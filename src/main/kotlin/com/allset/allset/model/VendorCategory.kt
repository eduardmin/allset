package com.allset.allset.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "vendor_categories")
data class VendorCategory(
    @Id val id: String? = null,
    val name: Map<String, String>,
    @Indexed(unique = true)
    val slug: String,
    val icon: String? = null,
    val sortOrder: Int = 0,
    val active: Boolean = true
)
