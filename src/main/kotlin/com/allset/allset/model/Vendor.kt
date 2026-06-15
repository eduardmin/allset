package com.allset.allset.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant

@Document(collection = "vendors")
data class Vendor(
    @Id val id: String? = null,
    val categoryId: String,
    val name: Map<String, String>,
    @Indexed(unique = true)
    val slug: String,
    val description: Map<String, String> = emptyMap(),
    val phone: String? = null,
    val email: String? = null,
    val address: Map<String, String>? = null,
    val city: String? = null,
    val website: String? = null,
    val socialLinks: SocialLinks? = null,
    val photos: List<String> = emptyList(),
    val coverImage: String? = null,
    val workingHours: Map<String, String>? = null,
    val rating: Double = 0.0,
    val budgetMin: BigDecimal? = null,
    val budgetMax: BigDecimal? = null,
    val languages: List<String> = emptyList(),
    val venueDetails: VenueDetails? = null,
    val active: Boolean = true,
    val featured: Boolean = false,
    val createdAt: Instant = Instant.now()
)

data class SocialLinks(
    val instagram: String? = null,
    val facebook: String? = null,
    val tiktok: String? = null
)

data class VenueDetails(
    val capacityMin: Int? = null,
    val capacityMax: Int? = null,
    val indoor: Boolean = true,
    val outdoor: Boolean = false,
    val areaSize: String? = null,
    val parking: Boolean = false,
    val accommodation: Boolean = false,
    val menuOptions: List<String> = emptyList(),
    val locationLink: String? = null
)
