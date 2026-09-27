package com.allset.allset.service

import com.allset.allset.model.AppliedPromoCode
import com.allset.allset.model.PromoCodeType
import com.allset.allset.model.User
import com.allset.allset.repository.PromoCodeRepository
import com.allset.allset.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Removes promo codes from a user's profile when they are no longer usable:
 * expired, deleted, or admin-deactivated. Applied single-use codes are kept,
 * because a single-use code is deactivated the moment it is applied and the
 * discount must remain available to the user until they pay.
 *
 * Kept dependency-light (repositories only) on purpose to avoid circular
 * dependencies with UserService / PromoCodeService.
 */
@Service
class PromoCodeCleanupService(
    private val promoCodeRepository: PromoCodeRepository,
    private val userRepository: UserRepository
) {

    fun pruneInvalidPromoCodes(user: User): User {
        if (user.appliedPromoCodes.isEmpty()) return user

        val now = Instant.now()
        val validCodes = user.appliedPromoCodes.filter { isStillValid(it, now) }

        if (validCodes.size == user.appliedPromoCodes.size) return user

        return userRepository.save(user.copy(appliedPromoCodes = validCodes))
    }

    private fun isStillValid(applied: AppliedPromoCode, now: Instant): Boolean {
        if (applied.expiresAt != null && applied.expiresAt.isBefore(now)) return false

        val promoCode = promoCodeRepository.findByCodeIgnoreCase(applied.code) ?: return false

        if (promoCode.expiresAt != null && promoCode.expiresAt.isBefore(now)) return false

        // Single-use codes are deactivated on apply, so an inactive single-use code
        // is expected and must be kept until the user pays.
        if (!promoCode.active && promoCode.type != PromoCodeType.SINGLE_USE) return false

        return true
    }
}
