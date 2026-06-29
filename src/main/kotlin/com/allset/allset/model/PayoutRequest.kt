package com.allset.allset.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant

@Document(collection = "payout_requests")
data class PayoutRequest(
    @Id val id: String? = null,
    @Indexed val designerId: String,
    val amount: BigDecimal,
    val status: PayoutStatus = PayoutStatus.REQUESTED,
    val method: String? = null,
    val details: String? = null,
    val adminNote: String? = null,
    // Ids of the earnings reserved/settled by this payout.
    val earningIds: List<String> = emptyList(),
    val requestedAt: Instant = Instant.now(),
    val processedAt: Instant? = null
)

enum class PayoutStatus {
    REQUESTED,
    APPROVED,
    PAID,
    REJECTED
}
