package com.allset.allset.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant

@Document(collection = "promo_codes")
data class PromoCode(
    @Id val id: String? = null,
    @Indexed(unique = true)
    val code: String,
    val discountType: DiscountType,
    val discountValue: BigDecimal,
    val active: Boolean = true,
    val startsAt: Instant? = null,
    val expiresAt: Instant? = null
)

data class AppliedPromoCode(
    val code: String,
    val discountType: DiscountType,
    val discountValue: BigDecimal,
    val expiresAt: Instant? = null,
    val appliedAt: Instant = Instant.now()
)

enum class DiscountType {
    PERCENTAGE,
    AMOUNT
}

fun PromoCode.toAppliedPromoCode(appliedAt: Instant = Instant.now()): AppliedPromoCode =
    AppliedPromoCode(
        code = code.uppercase(),
        discountType = discountType,
        discountValue = discountValue,
        expiresAt = expiresAt,
        appliedAt = appliedAt
    )
