package com.allset.allset.dto

import com.allset.allset.model.Vendor
import com.allset.allset.model.VendorCategory
import com.allset.allset.model.VendorSubcategory
import com.allset.allset.model.VendorType

/**
 * Public vendor detail response. Includes the full vendor plus the resolved
 * taxonomy (category / subcategory / type) and an aggregated gallery that
 * merges the cover image, vendor photos, explicit gallery photos and every
 * venue space's cover and images.
 */
data class VendorDetailResponse(
    val vendor: Vendor,
    val category: VendorCategory?,
    val subcategory: VendorSubcategory?,
    val type: VendorType?,
    val gallery: List<String>
)

fun buildVendorGallery(vendor: Vendor): List<String> {
    val images = mutableListOf<String>()
    vendor.coverImage?.let { images.add(it) }
    images.addAll(vendor.photos)
    images.addAll(vendor.gallery)
    vendor.venueDetails?.spaces?.forEach { space ->
        space.coverImage?.let { images.add(it) }
        images.addAll(space.images)
    }
    return images.filter { it.isNotBlank() }.distinct()
}
