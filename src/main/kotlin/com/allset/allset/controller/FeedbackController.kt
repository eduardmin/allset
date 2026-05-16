package com.allset.allset.controller

import com.allset.allset.model.FeedbackItem
import com.allset.allset.service.HomeService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/feedbacks")
class FeedbackController(
    private val homeService: HomeService
) {
    @GetMapping
    fun getAllFeedbacks(): List<FeedbackItem> = homeService.getFeedbacks()
}
