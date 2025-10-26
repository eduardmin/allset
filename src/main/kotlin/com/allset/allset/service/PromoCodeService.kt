package com.allset.allset.service

import com.allset.allset.dto.PricingSummary
import com.allset.allset.model.DiscountType
import com.allset.allset.model.PromoCode
import com.allset.allset.model.toAppliedPromoCode
import com.allset.allset.repository.PromoCodeRepository
import com.allset.allset.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.Instant

@Service
class PromoCodeService(
    private val promoCodeRepository: PromoCodeRepository,
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val pricingService: PricingService
) {

    fun applyPromoCodeToCurrentUser(code: String): PricingSummary {
        val promoCode = findAndValidatePromoCode(code)
        val user = userService.getCurrentUser()

        val appliedPromoCode = promoCode.toAppliedPromoCode()
        val updatedUser = user.copy(appliedPromoCode = appliedPromoCode)
        userRepository.save(updatedUser)

        return pricingService.summarize(appliedPromoCode)
    }

    fun previewPromoCode(code: String): PricingSummary {
        val promoCode = findAndValidatePromoCode(code)
        return pricingService.summarize(promoCode)
    }

    fun clearPromoCodeForCurrentUser(): PricingSummary {
        val user = userService.getCurrentUser()
        if (user.appliedPromoCode == null) {
            return pricingService.basePricing()
        }

        val updatedUser = user.copy(appliedPromoCode = null)
        userRepository.save(updatedUser)
        return pricingService.basePricing()
    }

    private fun findAndValidatePromoCode(rawCode: String): PromoCode {
        val normalizedCode = rawCode.trim()
        if (normalizedCode.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Promo code must not be blank.")
        }

        val promoCode = promoCodeRepository.findByCodeIgnoreCase(normalizedCode)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Promo code not found.")

        validatePromoCode(promoCode)
        return promoCode
    }

    private fun validatePromoCode(promoCode: PromoCode) {
        val now = Instant.now()

        if (!promoCode.active) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Promo code is inactive.")
        }

        if (promoCode.startsAt?.isAfter(now) == true) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Promo code is not active yet.")
        }

        if (promoCode.expiresAt?.isBefore(now) == true) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Promo code has expired.")
        }

        if (promoCode.discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Promo code discount must be greater than zero.")
        }

        if (promoCode.discountType == DiscountType.PERCENTAGE && promoCode.discountValue.compareTo(BigDecimal(100)) > 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Promo code percentage cannot exceed 100%.")
        }
    }
}
