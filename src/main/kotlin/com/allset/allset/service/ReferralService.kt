package com.allset.allset.service

import com.allset.allset.model.*
import com.allset.allset.repository.PromoCodeRepository
import com.allset.allset.repository.ReferralRepository
import com.allset.allset.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Service
class ReferralService(
    private val referralRepository: ReferralRepository,
    private val userRepository: UserRepository,
    private val promoCodeRepository: PromoCodeRepository,
    private val authenticationService: AuthenticationService
) {
    private val logger = LoggerFactory.getLogger(ReferralService::class.java)

    fun registerReferral(referralCode: String): Referral {
        val currentUserId = authenticationService.getCurrentUserId()
        val currentUser = userRepository.findById(currentUserId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
        }

        if (currentUser.referredBy != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already used a referral code.")
        }

        if (currentUser.referralCode.equals(referralCode, ignoreCase = true)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot use your own referral code.")
        }

        val referrer = userRepository.findByReferralCode(referralCode)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Referral code not found.")

        userRepository.save(currentUser.copy(referredBy = referralCode))

        val referral = Referral(
            referrerUserId = referrer.id!!,
            referredUserId = currentUserId,
            referralCode = referralCode
        )
        val saved = referralRepository.save(referral)

        logger.info("Referral registered: referrer=${referrer.id}, referred=$currentUserId, code=$referralCode")
        return saved
    }

    fun rewardReferrerForPayment(payingUserId: String) {
        val payingUser = userRepository.findById(payingUserId).orElse(null) ?: return
        if (payingUser.referredBy == null) return

        val pendingReferral = referralRepository.findByReferredUserIdAndStatus(
            payingUserId, ReferralStatus.PENDING
        ) ?: return

        val referrer = userRepository.findByReferralCode(payingUser.referredBy) ?: run {
            logger.warn("Referrer not found for code=${payingUser.referredBy}")
            return
        }

        val rewardCode = "REF-REWARD-${UUID.randomUUID().toString().take(8).uppercase()}"
        val promoCode = PromoCode(
            code = rewardCode,
            discountType = DiscountType.PERCENTAGE,
            discountValue = BigDecimal(5),
            type = PromoCodeType.SINGLE_USE,
            businessName = "allset-referral"
        )
        val savedPromo = promoCodeRepository.save(promoCode)

        val appliedPromo = savedPromo.toAppliedPromoCode()
        val updatedPromoCodes = referrer.appliedPromoCodes.plus(appliedPromo)
        userRepository.save(referrer.copy(appliedPromoCodes = updatedPromoCodes))

        referralRepository.save(
            pendingReferral.copy(
                status = ReferralStatus.REWARDED,
                rewardPromoCodeId = savedPromo.id,
                rewardedAt = Instant.now()
            )
        )

        logger.info("Referral reward given: referrer=${referrer.id}, code=$rewardCode, referral=${pendingReferral.id}")
    }
}
