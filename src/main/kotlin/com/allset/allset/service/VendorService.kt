package com.allset.allset.service

import com.allset.allset.dto.VendorDetailResponse
import com.allset.allset.dto.buildVendorGallery
import com.allset.allset.model.Vendor
import com.allset.allset.model.VendorCategory
import com.allset.allset.model.VendorStatus
import com.allset.allset.model.VendorSubcategory
import com.allset.allset.model.VendorType
import com.allset.allset.repository.VendorCategoryRepository
import com.allset.allset.repository.VendorRepository
import com.allset.allset.repository.VendorSubcategoryRepository
import com.allset.allset.repository.VendorTypeRepository
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
    private val vendorSubcategoryRepository: VendorSubcategoryRepository,
    private val vendorTypeRepository: VendorTypeRepository,
    private val mongoTemplate: MongoTemplate
) {

    fun getActiveCategories(): List<VendorCategory> {
        return vendorCategoryRepository.findAllByActiveTrueOrderBySortOrderAsc()
    }

    fun getActiveSubcategories(categorySlug: String?): List<VendorSubcategory> {
        if (categorySlug == null) {
            return vendorSubcategoryRepository.findAllByActiveTrueOrderBySortOrderAsc()
        }
        val category = vendorCategoryRepository.findBySlug(categorySlug)
            ?: return emptyList()
        return vendorSubcategoryRepository.findAllByCategoryIdAndActiveTrueOrderBySortOrderAsc(category.id!!)
    }

    fun getActiveTypes(subcategorySlug: String?): List<VendorType> {
        if (subcategorySlug == null) {
            return vendorTypeRepository.findAllByActiveTrueOrderBySortOrderAsc()
        }
        val subcategory = vendorSubcategoryRepository.findBySlug(subcategorySlug)
            ?: return emptyList()
        return vendorTypeRepository.findAllBySubcategoryIdAndActiveTrueOrderBySortOrderAsc(subcategory.id!!)
    }

    fun getVendorDetailBySlug(slug: String): VendorDetailResponse {
        val vendor = vendorRepository.findBySlug(slug)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found")

        if (vendor.status != VendorStatus.APPROVED || !vendor.active) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found")
        }

        val category = vendorCategoryRepository.findById(vendor.categoryId).orElse(null)
        val subcategory = vendor.subcategoryId?.let { vendorSubcategoryRepository.findById(it).orElse(null) }
        val type = vendor.typeId?.let { vendorTypeRepository.findById(it).orElse(null) }

        return VendorDetailResponse(
            vendor = vendor,
            category = category,
            subcategory = subcategory,
            type = type,
            gallery = buildVendorGallery(vendor)
        )
    }

    fun searchVendors(
        categorySlug: String?,
        subcategorySlug: String?,
        typeSlug: String?,
        city: String?,
        minBudget: BigDecimal?,
        maxBudget: BigDecimal?,
        priceLevel: Int?,
        minRating: Double?,
        guestCount: Int?,
        language: String?,
        top: Boolean?,
        search: String?,
        sort: String?,
        page: Int,
        size: Int
    ): Page<Vendor> {
        val criteria = Criteria.where("active").`is`(true)
            .and("status").`is`(VendorStatus.APPROVED.name)

        if (categorySlug != null) {
            val category = vendorCategoryRepository.findBySlug(categorySlug)
            if (category != null) {
                criteria.and("categoryId").`is`(category.id)
            }
        }

        if (subcategorySlug != null) {
            val subcategory = vendorSubcategoryRepository.findBySlug(subcategorySlug)
            if (subcategory != null) {
                criteria.and("subcategoryId").`is`(subcategory.id)
            }
        }

        if (typeSlug != null) {
            val type = vendorTypeRepository.findBySlug(typeSlug)
            if (type != null) {
                criteria.and("typeId").`is`(type.id)
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

        if (priceLevel != null) {
            criteria.and("priceLevel").`is`(priceLevel)
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

        if (top != null) {
            criteria.and("top").`is`(top)
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
            else -> Sort.by(Sort.Direction.DESC, "top").and(Sort.by(Sort.Direction.DESC, "rating"))
        }

        val pageable = PageRequest.of(page, size, sorting)
        val query = Query(criteria).with(pageable)

        val total = mongoTemplate.count(Query(criteria), Vendor::class.java)
        val vendors = mongoTemplate.find(query, Vendor::class.java)

        return PageImpl(vendors, pageable, total)
    }
}
