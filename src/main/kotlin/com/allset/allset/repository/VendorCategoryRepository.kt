package com.allset.allset.repository

import com.allset.allset.model.VendorCategory
import org.springframework.data.mongodb.repository.MongoRepository

interface VendorCategoryRepository : MongoRepository<VendorCategory, String> {
    fun findBySlug(slug: String): VendorCategory?
    fun findAllByActiveTrue(): List<VendorCategory>
    fun findAllByActiveTrueOrderBySortOrderAsc(): List<VendorCategory>
}
