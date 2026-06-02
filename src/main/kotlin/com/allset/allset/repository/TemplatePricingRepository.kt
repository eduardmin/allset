package com.allset.allset.repository

import com.allset.allset.model.TemplatePricing
import org.springframework.data.mongodb.repository.MongoRepository

interface TemplatePricingRepository : MongoRepository<TemplatePricing, String> {
    fun findByTemplateId(templateId: String): TemplatePricing?
}
