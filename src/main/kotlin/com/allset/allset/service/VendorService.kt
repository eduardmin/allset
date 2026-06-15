package com.allset.allset.service

import com.allset.allset.model.Vendor
import com.allset.allset.model.VendorCategory
import com.allset.allset.repository.VendorCategoryRepository
import com.allset.allset.repository.VendorRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

@Service
class VendorService(
    private val vendorRepository: VendorRepository,
    private val vendorCategoryRepository: VendorCategoryRepository,
    private val mongoTemplate: MongoTemplate
) {

    fun getActiveCategories(): List<VendorCategory> {
        return vendorCategoryRepository.findAllByActiveTrueOrderBySortOrderAsc()
    }

    fun getVendorBySlug(slug: String): Vendor {
        return vendorRepository.findBySlug(slug)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found")
    }

    fun searchVendors(
        categorySlug: String?,
        city: String?,
        minBudget: BigDecimal?,
        maxBudget: BigDecimal?,
        minRating: Double?,
        guestCount: Int?,
        language: String?,
        search: String?,
        sort: String?,
        page: Int,
        size: Int
    ): Page<Vendor> {
        val criteria = Criteria.where("active").`is`(true)

        if (categorySlug != null) {
            val category = vendorCategoryRepository.findBySlug(categorySlug)
            if (category != null) {
                criteria.and("categoryId").`is`(category.id)
            }
        }

        if (city != null) {
            criteria.and("city").regex(city, "i")
        }

        if (minBudget != null) {
            criteria.and("budgetMax").gte(minBudget)
        }

        if (maxBudget != null) {
            criteria.and("budgetMin").lte(maxBudget)
        }

        if (minRating != null) {
            criteria.and("rating").gte(minRating)
        }

        if (guestCount != null) {
            criteria.and("venueDetails.capacityMin").lte(guestCount)
            criteria.and("venueDetails.capacityMax").gte(guestCount)
        }

        if (language != null) {
            criteria.and("languages").`in`(language)
        }

        if (search != null && search.isNotBlank()) {
            val searchRegex = search.trim()
            criteria.orOperator(
                Criteria.where("name.en").regex(searchRegex, "i"),
                Criteria.where("name.hy").regex(searchRegex, "i"),
                Criteria.where("description.en").regex(searchRegex, "i"),
                Criteria.where("description.hy").regex(searchRegex, "i")
            )
        }

        val sorting = when (sort) {
            "rating" -> Sort.by(Sort.Direction.DESC, "rating")
            "budget_asc" -> Sort.by(Sort.Direction.ASC, "budgetMin")
            "budget_desc" -> Sort.by(Sort.Direction.DESC, "budgetMin")
            "name" -> Sort.by(Sort.Direction.ASC, "name.en")
            else -> Sort.by(Sort.Direction.DESC, "featured").and(Sort.by(Sort.Direction.DESC, "rating"))
        }

        val pageable = PageRequest.of(page, size, sorting)
        val query = Query(criteria).with(pageable)

        val total = mongoTemplate.count(Query(criteria), Vendor::class.java)
        val vendors = mongoTemplate.find(query, Vendor::class.java)

        return PageImpl(vendors, pageable, total)
    }
}
