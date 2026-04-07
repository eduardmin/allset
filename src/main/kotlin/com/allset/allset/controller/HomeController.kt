package com.allset.allset.controller

import com.allset.allset.model.HomeResponse
import com.allset.allset.service.AuthenticationService
import com.allset.allset.service.HomeService
import com.allset.allset.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/home")
class HomeController(
    private val homeService: HomeService,
    private val authenticationService: AuthenticationService,
    private val userService: UserService
) {

    @GetMapping
    fun getHomeData(): HomeResponse {
        try {
            val userId = authenticationService.getCurrentUserIdOrNull()
            if (userId != null) {
                userService.updateLastSeen(userId)
            }
        } catch (_: Exception) { }

        return homeService.getHomeData()
    }
}
