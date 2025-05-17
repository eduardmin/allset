package com.allset.allset.controller

import com.allset.allset.model.User
import com.allset.allset.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(private val userService: UserService) {

    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @GetMapping("/login")
    fun login(@AuthenticationPrincipal jwt: Jwt?): User {
        logger.info("📥 Received login request at /api/auth/login")

        if (jwt == null) {
            throw RuntimeException("🚨 Authentication failed. No valid token provided.")
        }

        // Save or update user and return user data
        return userService.saveUser(jwt)
    }
}
