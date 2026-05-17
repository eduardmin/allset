package com.allset.allset.controller

import com.allset.allset.dto.PricingSummary
import com.allset.allset.dto.PromoCodeRequest
import com.allset.allset.model.AppliedPromoCode
import com.allset.allset.service.PromoCodeService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/promo-codes")
class PromoCodeController(
    private val promoCodeService: PromoCodeService
) {

    @PostMapping("/apply")
    fun applyPromoCode(@RequestBody request: PromoCodeRequest): AppliedPromoCode {
        return promoCodeService.applyPromoCodeToCurrentUser(request.code)
    }

    @DeleteMapping("/apply")
    fun clearPromoCode(): PricingSummary {
        return promoCodeService.clearPromoCodeForCurrentUser()
    }

    @PostMapping("/preview")
    fun previewPromoCode(@RequestBody request: PromoCodeRequest): PricingSummary {
        return promoCodeService.previewPromoCode(request.code)
    }

    @GetMapping("/active")
    fun getActivePromoCodes(): List<AppliedPromoCode> {
        return promoCodeService.getActivePromoCodes()
    }
}
