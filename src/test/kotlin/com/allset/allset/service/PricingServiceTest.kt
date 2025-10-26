package com.allset.allset.service

import com.allset.allset.config.PricingProperties
import com.allset.allset.model.AppliedPromoCode
import com.allset.allset.model.DiscountType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import kotlin.random.Random

class PricingServiceTest {

    private fun pricingService(basePrice: BigDecimal = BigDecimal("100"), random: Random = Random(0)):
        PricingService {
        val pricingProperties = PricingProperties().apply { this.basePrice = basePrice }
        return PricingService(pricingProperties, random)
    }

    @Test
    fun `selects promo code with highest discount amount`() {
        val service = pricingService()
        val promoCodes = listOf(
            AppliedPromoCode(
                code = "PERCENT20",
                discountType = DiscountType.PERCENTAGE,
                discountValue = BigDecimal("20")
            ),
            AppliedPromoCode(
                code = "AMOUNT15",
                discountType = DiscountType.AMOUNT,
                discountValue = BigDecimal("15")
            )
        )

        val summary = service.summarize(promoCodes)

        assertEquals("PERCENT20", summary.promoCode?.code)
        assertEquals(BigDecimal("20.00"), summary.discountAmount)
        assertEquals(BigDecimal("80.00"), summary.finalPrice)
    }

    @Test
    fun `prefers promo code that expires sooner when discounts are equal`() {
        val service = pricingService()
        val sooner = Instant.now().plusSeconds(1_000)
        val later = Instant.now().plusSeconds(10_000)
        val promoCodes = listOf(
            AppliedPromoCode(
                code = "AMOUNT30_LATE",
                discountType = DiscountType.AMOUNT,
                discountValue = BigDecimal("30"),
                expiresAt = later
            ),
            AppliedPromoCode(
                code = "AMOUNT30_SOON",
                discountType = DiscountType.AMOUNT,
                discountValue = BigDecimal("30"),
                expiresAt = sooner
            )
        )

        val summary = service.summarize(promoCodes)

        assertEquals("AMOUNT30_SOON", summary.promoCode?.code)
    }

    @Test
    fun `breaks ties randomly when discounts and expirations are equal`() {
        val deterministicRandom = Random(42)
        val service = pricingService(random = deterministicRandom)
        val promoCodes = listOf(
            AppliedPromoCode(
                code = "PERCENT20",
                discountType = DiscountType.PERCENTAGE,
                discountValue = BigDecimal("20")
            ),
            AppliedPromoCode(
                code = "AMOUNT20",
                discountType = DiscountType.AMOUNT,
                discountValue = BigDecimal("20")
            )
        )

        val summary = service.summarize(promoCodes)

        assertEquals("AMOUNT20", summary.promoCode?.code)
    }
}
