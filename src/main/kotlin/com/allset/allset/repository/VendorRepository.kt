package com.allset.allset.repository

import com.allset.allset.model.Vendor
import org.springframework.data.mongodb.repository.MongoRepository

interface VendorRepository : MongoRepository<Vendor, String> {
    fun findBySlug(slug: String): Vendor?
    fun findAllByCategoryId(categoryId: String): List<Vendor>
    fun findAllByActiveTrue(): List<Vendor>
    fun findAllByCategoryIdAndActiveTrue(categoryId: String): List<Vendor>
}
