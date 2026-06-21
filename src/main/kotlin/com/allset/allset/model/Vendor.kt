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
    val subcategoryId: String? = null,
    val typeId: String? = null,
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
    val gallery: List<String> = emptyList(),
    val coverImage: String? = null,
    val workingHours: Map<String, String>? = null,
    val rating: Double = 0.0,
    val budgetMin: BigDecimal? = null,
    val budgetMax: BigDecimal? = null,
    // General affordability tier: 1 = $ (cheap) ... 5 = $$$$$ (expensive)
    val priceLevel: Int? = null,
    val languages: List<String> = emptyList(),
    val venueDetails: VenueDetails? = null,
    val status: VendorStatus = VendorStatus.MODERATION,
    val active: Boolean = true,
    val top: Boolean = false,
    val createdAt: Instant = Instant.now()
)

enum class VendorStatus {
    MODERATION,
    APPROVED,
    REJECTED
}

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
    val locationLink: String? = null,
    val spaces: List<VenueSpace> = emptyList()
)

/**
 * A distinct area within a venue (e.g. indoor hall, outdoor garden),
 * each with its own capacity, attributes and images.
 */
data class VenueSpace(
    val id: String? = null,
    val name: Map<String, String> = emptyMap(),
    // Free-form area type, e.g. "indoor", "outdoor", "terrace"
    val type: String? = null,
    val capacityMin: Int? = null,
    val capacityMax: Int? = null,
    val areaSize: String? = null,
    val indoor: Boolean = true,
    val outdoor: Boolean = false,
    val description: Map<String, String> = emptyMap(),
    val coverImage: String? = null,
    val images: List<String> = emptyList()
)
