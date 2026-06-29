package com.allset.allset.repository

import com.allset.allset.model.DesignerEarning
import com.allset.allset.model.EarningStatus
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface DesignerEarningRepository : MongoRepository<DesignerEarning, String> {
    fun findAllByDesignerIdOrderByCreatedAtDesc(designerId: String): List<DesignerEarning>
    fun findAllByDesignerIdAndStatus(designerId: String, status: EarningStatus): List<DesignerEarning>
    fun existsByPaymentId(paymentId: String): Boolean
    fun countByLinkedTemplateId(linkedTemplateId: String): Long
}
