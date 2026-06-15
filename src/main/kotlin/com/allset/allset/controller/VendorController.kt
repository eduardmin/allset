package com.allset.allset.controller

import com.allset.allset.model.Vendor
import com.allset.allset.model.VendorCategory
import com.allset.allset.service.VendorService
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/vendors")
class VendorController(
    private val vendorService: VendorService
) {

    @GetMapping("/categories")
    fun getCategories(): List<VendorCategory> {
        return vendorService.getActiveCategories()
    }

    @GetMapping
    fun searchVendors(
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) city: String?,
        @RequestParam(required = false) minBudget: BigDecimal?,
        @RequestParam(required = false) maxBudget: BigDecimal?,
        @RequestParam(required = false) minRating: Double?,
        @RequestParam(required = false) guestCount: Int?,
        @RequestParam(required = false) language: String?,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) sort: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Page<Vendor> {
        return vendorService.searchVendors(
            categorySlug = category,
            city = city,
            minBudget = minBudget,
            maxBudget = maxBudget,
            minRating = minRating,
            guestCount = guestCount,
            language = language,
            search = search,
            sort = sort,
            page = page,
            size = size
        )
    }

    @GetMapping("/{slug}")
    fun getVendorBySlug(@PathVariable slug: String): Vendor {
        return vendorService.getVendorBySlug(slug)
    }
}
