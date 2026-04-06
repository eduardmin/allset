package com.allset.allset.dto

import com.allset.allset.model.DiscountType
import java.math.BigDecimal
import java.time.Instant

data class CreatePromoCodeRequest(
    val code: String,
    val discountType: DiscountType,
    val discountValue: BigDecimal,
    val active: Boolean = true,
    val startsAt: Instant? = null,
    val expiresAt: Instant? = null
)

data class UpdatePromoCodeRequest(
    val code: String? = null,
    val discountType: DiscountType? = null,
    val discountValue: BigDecimal? = null,
    val active: Boolean? = null,
    val startsAt: Instant? = null,
    val expiresAt: Instant? = null
)
