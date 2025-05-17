package com.allset.allset.service

import com.allset.allset.repository.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val userRepository: UserRepository
) {
    fun getCurrentUserId(): String {
        val email = getCurrentUserEmail()
        val user = userRepository.findByEmail(email)
            ?: throw RuntimeException("🚨 User with email $email not found.")
        return user.id ?: throw RuntimeException("🚨 User ID is null for email $email")
    }

    private fun getCurrentUserEmail(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        val jwt = authentication.principal as Jwt
        return jwt.getClaim("email")
    }
}
