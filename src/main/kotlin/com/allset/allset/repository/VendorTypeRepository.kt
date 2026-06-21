package com.allset.allset.repository

import com.allset.allset.model.VendorType
import org.springframework.data.mongodb.repository.MongoRepository

interface VendorTypeRepository : MongoRepository<VendorType, String> {
    fun findBySlug(slug: String): VendorType?
    fun findAllBySubcategoryId(subcategoryId: String): List<VendorType>
    fun findAllByActiveTrueOrderBySortOrderAsc(): List<VendorType>
    fun findAllBySubcategoryIdAndActiveTrueOrderBySortOrderAsc(subcategoryId: String): List<VendorType>
}
