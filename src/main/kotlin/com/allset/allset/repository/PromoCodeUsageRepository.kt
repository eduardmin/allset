package com.allset.allset.repository

import com.allset.allset.model.PromoCodeUsage
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface PromoCodeUsageRepository : MongoRepository<PromoCodeUsage, String> {
    fun findAllByPromoCodeId(promoCodeId: String): List<PromoCodeUsage>
    fun findAllByCode(code: String): List<PromoCodeUsage>
    fun findAllByBusinessName(businessName: String): List<PromoCodeUsage>
    fun countByPromoCodeId(promoCodeId: String): Long
    fun countByBusinessName(businessName: String): Long
}
