package com.allset.allset.dto

import com.allset.allset.model.AppliedPromoCode
import java.math.BigDecimal

data class PricingSummary(
    val basePrice: BigDecimal,
    val finalPrice: BigDecimal,
    val discountAmount: BigDecimal,
    val promoCode: AppliedPromoCode?
)
