package com.allset.allset.controller

import com.allset.allset.model.HomeResponse
import com.allset.allset.service.HomeService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/home")
class HomeController(
    private val homeService: HomeService
) {

    @GetMapping
    fun getHomeData(): HomeResponse {
        return homeService.getHomeData()
    }
}
