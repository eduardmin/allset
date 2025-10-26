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
        return getCurrentUserIdOrNull()
            ?: throw RuntimeException("🚨 User not authenticated.")
    }

    fun getCurrentUserIdOrNull(): String? {
        val authentication = SecurityContextHolder.getContext().authentication ?: return null
        val jwt = authentication.principal as? Jwt ?: return null
        val email = jwt.getClaim<String>("email") ?: return null
        val user = userRepository.findByEmail(email) ?: return null
        return user.id
    }
}
