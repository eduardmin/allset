package com.allset.allset.repository

import com.allset.allset.model.Payment
import com.allset.allset.model.PaymentStatus
import org.springframework.data.mongodb.repository.MongoRepository

interface PaymentRepository : MongoRepository<Payment, String> {
    fun findByBillNo(billNo: String): Payment?
    fun findAllByUserId(userId: String): List<Payment>
    fun findFirstByUserIdAndStatusOrderByCompletedAtDesc(userId: String, status: PaymentStatus): Payment?
    fun findFirstByUserIdOrderByCreatedAtDesc(userId: String): Payment?
    fun findAllByUserIdAndStatus(userId: String, status: PaymentStatus): List<Payment>
}
