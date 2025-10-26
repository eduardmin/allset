package com.allset.allset.config

import com.allset.allset.model.PromoCode
import com.allset.allset.repository.PromoCodeRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PromoCodeInitializer(
    private val promoCodeProperties: PromoCodeProperties,
    private val promoCodeRepository: PromoCodeRepository
) {

    private val logger = LoggerFactory.getLogger(PromoCodeInitializer::class.java)

    @PostConstruct
    fun syncPromoCodes() {
        promoCodeProperties.codes.forEach { definition ->
            if (definition.code.isBlank()) {
                logger.warn("Skipping promo code with blank code in configuration")
                return@forEach
            }

            val existing = promoCodeRepository.findByCodeIgnoreCase(definition.code)
            val promoCode = PromoCode(
                id = existing?.id,
                code = definition.code.trim().uppercase(),
                discountType = definition.discountType,
                discountValue = definition.discountValue,
                active = definition.active,
                startsAt = definition.startsAt,
                expiresAt = definition.expiresAt
            )

            if (existing == null) {
                promoCodeRepository.save(promoCode)
                logger.info("Created promo code ${definition.code} from configuration")
            } else if (existing != promoCode) {
                promoCodeRepository.save(promoCode)
                logger.info("Updated promo code ${definition.code} from configuration")
            }
        }
    }
}
