package com.allset.allset.repository

import com.allset.allset.model.PromoCode
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface PromoCodeRepository : MongoRepository<PromoCode, String> {
    fun findByCodeIgnoreCase(code: String): PromoCode?
}
