package com.allset.allset.service

import com.allset.allset.model.User
import com.allset.allset.repository.UserRepository
import com.allset.allset.util.EmailUtils
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
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

        val normalizedEmail = EmailUtils.normalize(email)

        val existing = userRepository.findByEmailIgnoreCase(normalizedEmail)
        val user = if (existing != null) {
            userRepository.save(existing.copy(sub = sub))
        } else {
            try {
                userRepository.save(User(sub = sub, email = normalizedEmail, name = name ?: "User", picture = picture))
            } catch (ex: DuplicateKeyException) {
                // A concurrent request created this user first; reuse and attach this sub.
                val raced = userRepository.findByEmailIgnoreCase(normalizedEmail) ?: throw ex
                userRepository.save(raced.copy(sub = sub))
            }
        }

        return user.id
    }
}
