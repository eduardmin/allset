package com.allset.allset.repository

import com.allset.allset.model.Payment
import org.springframework.data.mongodb.repository.MongoRepository

interface PaymentRepository : MongoRepository<Payment, String> {
    fun findByBillNo(billNo: String): Payment?
    fun findAllByUserId(userId: String): List<Payment>
}
