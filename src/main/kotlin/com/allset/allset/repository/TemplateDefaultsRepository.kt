package com.allset.allset.repository

import com.allset.allset.model.TemplateDefaultsConfig
import org.springframework.data.mongodb.repository.MongoRepository

interface TemplateDefaultsRepository : MongoRepository<TemplateDefaultsConfig, String> {
    fun findByTemplateId(templateId: String): TemplateDefaultsConfig?
}
