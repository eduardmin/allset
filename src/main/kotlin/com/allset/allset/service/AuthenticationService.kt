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

        val sub = jwt.getClaim<String>("sub") ?: return null

        val existingUser = userRepository.findBySub(sub)
        if (existingUser != null) {
            return existingUser.id
        }

        var email = jwt.getClaim<String>("email")
        var name = jwt.getClaim<String>("name") ?: jwt.getClaim<String>("nickname")
        var picture = jwt.getClaim<String>("picture")

        if (email == null) {
            logger.info("New user, fetching profile from Auth0 UserInfo...")
            val userInfo = auth0UserInfoService.getUserInfo(jwt.tokenValue)
            if (userInfo != null) {
                email = userInfo.email
                name = userInfo.name ?: userInfo.nickname
                picture = userInfo.picture
            }
        }

        if (email == null) {
            logger.warn("No email found in JWT or UserInfo for sub: $sub")
            return null
        }

        var user = userRepository.findByEmail(email)
        if (user != null) {
            user = userRepository.save(user.copy(sub = sub))
        } else {
            user = userRepository.save(User(sub = sub, email = email, name = name ?: "User", picture = picture))
        }

        return user.id
    }
}
