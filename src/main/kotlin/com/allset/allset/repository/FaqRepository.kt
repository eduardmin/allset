package com.allset.allset.repository

import com.allset.allset.model.FaqItem
import org.springframework.data.mongodb.repository.MongoRepository

interface FaqRepository : MongoRepository<FaqItem, String> {
    fun findAllByOrderBySortOrderAsc(): List<FaqItem>
}
