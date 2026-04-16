package com.allset.allset.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant

@Document(collection = "payments")
data class Payment(
    @Id val id: String? = null,
    val userId: String,
    val invitationId: String? = null,
    val amount: BigDecimal,
    @Indexed(unique = true)
    val billNo: String,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val transactionId: String? = null,
    val payerAccount: String? = null,
    val transactionDate: String? = null,
    val createdAt: Instant = Instant.now(),
    val completedAt: Instant? = null
)

enum class PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED
}
