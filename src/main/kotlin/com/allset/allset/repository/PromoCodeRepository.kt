package com.allset.allset.repository

import com.allset.allset.model.PromoCode
import com.allset.allset.model.PromoCodeType
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface PromoCodeRepository : MongoRepository<PromoCode, String> {
    fun findByCodeIgnoreCase(code: String): PromoCode?
    fun countByActiveTrue(): Long
    fun findAllByType(type: PromoCodeType): List<PromoCode>
    fun findAllByBusinessName(businessName: String): List<PromoCode>
    fun findAllByBusinessNameIgnoreCase(businessName: String): List<PromoCode>
}
