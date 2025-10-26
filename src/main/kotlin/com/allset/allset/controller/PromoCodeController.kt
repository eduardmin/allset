package com.allset.allset.controller

import com.allset.allset.dto.PricingSummary
import com.allset.allset.dto.PromoCodeRequest
import com.allset.allset.service.PromoCodeService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/promo-codes")
class PromoCodeController(
    private val promoCodeService: PromoCodeService
) {

    @PostMapping("/apply")
    fun applyPromoCode(@RequestBody request: PromoCodeRequest): PricingSummary {
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
}
