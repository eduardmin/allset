package com.allset.allset.service

import com.allset.allset.model.User
import com.allset.allset.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val userRepository: UserRepository,
    private val auth0UserInfoService: Auth0UserInfoService
) {
    private val logger = LoggerFactory.getLogger(AuthenticationService::class.java)

    fun getCurrentUserId(): String {
        return getCurrentUserIdOrNull()
            ?: throw RuntimeException("🚨 User not authenticated.")
    }

    fun getCurrentUserIdOrNull(): String? {
        val authentication = SecurityContextHolder.getContext().authentication ?: return null
        val jwt = authentication.principal as? Jwt ?: return null
        
        logger.info("🔍 JWT Claims: ${jwt.claims}")
        
        // Try email from JWT token first
        var email = jwt.getClaim<String>("email")
        var name = jwt.getClaim<String>("name") ?: jwt.getClaim<String>("nickname")
        var picture = jwt.getClaim<String>("picture")
        
        // If email not in JWT, fetch from Auth0 UserInfo endpoint
        if (email == null) {
            logger.info("📧 Email not in JWT, fetching from Auth0 UserInfo endpoint...")
            val accessToken = jwt.tokenValue
            val userInfo = auth0UserInfoService.getUserInfo(accessToken)
            
            if (userInfo != null) {
                email = userInfo.email
                name = userInfo.name ?: userInfo.nickname
                picture = userInfo.picture
                logger.info("✅ Retrieved email from UserInfo: $email")
            } else {
                logger.warn("⚠️ Could not fetch user info from Auth0")
            }
        }
        
        if (email != null) {
            logger.info("📧 Using email: $email")
            var user = userRepository.findByEmail(email)
            
            // Auto-create user if doesn't exist
            if (user == null) {
                logger.info("🆕 User not found, creating new user with email: $email")
                val newUser = User(
                    email = email, 
                    name = name ?: "User", 
                    picture = picture
                )
                user = userRepository.save(newUser)
                logger.info("✅ Created new user: ${user.id}")
            }
            
            return user.id
        }
        
        logger.warn("⚠️ No email found in JWT or UserInfo")
        return null
    }
}
