package com.allset.allset.controller

import com.allset.allset.model.FaqItem
import com.allset.allset.service.HomeService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/faqs")
class FaqController(
    private val homeService: HomeService
) {
    @GetMapping
    fun getAllFaqs(): List<FaqItem> = homeService.getFaqs()
}
