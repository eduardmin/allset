package com.allset.allset.service

import com.allset.allset.config.PricingProperties
import com.allset.allset.dto.PricingSummary
import com.allset.allset.model.AppliedPromoCode
import com.allset.allset.model.DiscountType
import com.allset.allset.model.PromoCode
import com.allset.allset.model.toAppliedPromoCode
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

@Service
class PricingService(
    private val pricingProperties: PricingProperties,
    private val random: Random = Random.Default
) {

    fun summarize(appliedPromoCode: AppliedPromoCode?): PricingSummary {
        val basePrice = pricingProperties.basePrice.setScale(2, RoundingMode.HALF_UP)
        val discountAmount = appliedPromoCode?.let { calculateDiscount(basePrice, it.discountType, it.discountValue) }
            ?: BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        val finalPrice = basePrice.subtract(discountAmount).coerceAtLeast(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)

        return PricingSummary(
            basePrice = basePrice,
            finalPrice = finalPrice,
            discountAmount = discountAmount,
            promoCode = appliedPromoCode
        )
    }

    fun summarize(appliedPromoCodes: Collection<AppliedPromoCode>): PricingSummary {
        val basePrice = pricingProperties.basePrice.setScale(2, RoundingMode.HALF_UP)
        val selectedPromoCode = appliedPromoCodes
            .takeUnless { it.isEmpty() }
            ?.let { selectBestPromoCode(basePrice, it) }

        val discountAmount = selectedPromoCode?.let { calculateDiscount(basePrice, it.discountType, it.discountValue) }
            ?: BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        val finalPrice = basePrice.subtract(discountAmount).coerceAtLeast(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)

        return PricingSummary(
            basePrice = basePrice,
            finalPrice = finalPrice,
            discountAmount = discountAmount,
            promoCode = selectedPromoCode
        )
    }

    fun summarize(promoCode: PromoCode): PricingSummary {
        return summarize(promoCode.toAppliedPromoCode())
    }

    fun basePricing(): PricingSummary = summarize(null)

    private fun calculateDiscount(basePrice: BigDecimal, discountType: DiscountType, discountValue: BigDecimal): BigDecimal {
        val normalizedDiscount = when (discountType) {
            DiscountType.PERCENTAGE -> basePrice.multiply(discountValue)
                .divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
            DiscountType.AMOUNT -> discountValue.setScale(2, RoundingMode.HALF_UP)
        }

        return normalizedDiscount
            .coerceAtLeast(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
            .coerceAtMost(basePrice)
    }

    private fun selectBestPromoCode(basePrice: BigDecimal, appliedPromoCodes: Collection<AppliedPromoCode>): AppliedPromoCode {
        val evaluatedPromoCodes = appliedPromoCodes.map { promoCode ->
            val discountAmount = calculateDiscount(basePrice, promoCode.discountType, promoCode.discountValue)
            promoCode to discountAmount
        }

        val bestDiscountAmount = evaluatedPromoCodes.maxOf { it.second }
        val bestPromoCodes = evaluatedPromoCodes.filter { it.second.compareTo(bestDiscountAmount) == 0 }
        if (bestPromoCodes.size == 1) {
            return bestPromoCodes.first().first
        }

        val expiringPromoCodes = bestPromoCodes.filter { it.first.expiresAt != null }
        if (expiringPromoCodes.isNotEmpty()) {
            val nearestExpiration = expiringPromoCodes.minOf { it.first.expiresAt!! }
            val nearestPromoCodes = expiringPromoCodes.filter { it.first.expiresAt == nearestExpiration }
            if (nearestPromoCodes.size == 1) {
                return nearestPromoCodes.first().first
            }
            return nearestPromoCodes.random(random).first
        }

        return bestPromoCodes.random(random).first
    }

    private fun BigDecimal.coerceAtLeast(minimum: BigDecimal): BigDecimal =
        if (this.compareTo(minimum) < 0) minimum else this

    private fun BigDecimal.coerceAtMost(maximum: BigDecimal): BigDecimal =
        if (this.compareTo(maximum) > 0) maximum else this
}
