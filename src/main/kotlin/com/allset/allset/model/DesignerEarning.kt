package com.allset.allset.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant

@Document(collection = "designer_earnings")
data class DesignerEarning(
    @Id val id: String? = null,
    @Indexed val designerId: String,
    val submissionId: String? = null,
    val linkedTemplateId: String? = null,
    @Indexed(unique = true) val paymentId: String,
    val invitationId: String? = null,
    val saleAmount: BigDecimal,
    val commissionRate: Double,
    val earnedAmount: BigDecimal,
    val status: EarningStatus = EarningStatus.PENDING,
    val createdAt: Instant = Instant.now(),
    val availableAt: Instant? = null,
    val paidAt: Instant? = null,
    val payoutRequestId: String? = null
)

enum class EarningStatus {
    PENDING,
    AVAILABLE,
    PAID
}
