package com.allset.allset.repository

import com.allset.allset.model.VendorSubcategory
import org.springframework.data.mongodb.repository.MongoRepository

interface VendorSubcategoryRepository : MongoRepository<VendorSubcategory, String> {
    fun findBySlug(slug: String): VendorSubcategory?
    fun findAllByCategoryId(categoryId: String): List<VendorSubcategory>
    fun findAllByActiveTrueOrderBySortOrderAsc(): List<VendorSubcategory>
    fun findAllByCategoryIdAndActiveTrueOrderBySortOrderAsc(categoryId: String): List<VendorSubcategory>
}
