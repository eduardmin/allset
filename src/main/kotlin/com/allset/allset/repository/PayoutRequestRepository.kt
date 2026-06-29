package com.allset.allset.repository

import com.allset.allset.model.PayoutRequest
import com.allset.allset.model.PayoutStatus
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface PayoutRequestRepository : MongoRepository<PayoutRequest, String> {
    fun findAllByDesignerIdOrderByRequestedAtDesc(designerId: String): List<PayoutRequest>
    fun findAllByStatusOrderByRequestedAtDesc(status: PayoutStatus): List<PayoutRequest>
    fun findAllByOrderByRequestedAtDesc(): List<PayoutRequest>
}
