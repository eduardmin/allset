package com.allset.allset.repository

import com.allset.allset.model.ApiErrorLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.Instant

interface ApiErrorLogRepository : MongoRepository<ApiErrorLog, String> {

    fun findAllByOrderByTimestampDesc(pageable: Pageable): Page<ApiErrorLog>

    fun findByStatusOrderByTimestampDesc(status: Int, pageable: Pageable): Page<ApiErrorLog>

    fun findByUserIdOrderByTimestampDesc(userId: String, pageable: Pageable): Page<ApiErrorLog>

    fun findByStatusAndUserIdOrderByTimestampDesc(status: Int, userId: String, pageable: Pageable): Page<ApiErrorLog>

    fun findByTimestampBetweenOrderByTimestampDesc(from: Instant, to: Instant, pageable: Pageable): Page<ApiErrorLog>
}
