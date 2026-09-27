package com.allset.allset.service

import com.allset.allset.dto.UpdateUserRequest
import com.allset.allset.model.*
import com.allset.allset.repository.InvitationRepository
import com.allset.allset.repository.UserRepository
import com.allset.allset.repository.ConfirmationRepository
import com.allset.allset.util.EmailUtils
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserService(
    private val userRepository: UserRepository,
    private val invitationRepository: InvitationRepository,
    private val confirmationRepository: ConfirmationRepository,
    private val authenticationService: AuthenticationService,
    private val promoCodeCleanupService: PromoCodeCleanupService
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    fun saveUser(jwt: Jwt): User {
        val rawEmail = jwt.getClaim<String>("email")
        val name = jwt.getClaim<String>("name") ?: jwt.getClaim<String>("nickname") ?: "User"
        val picture = jwt.getClaim<String>("picture")
        val sub = jwt.getClaim<String>("sub")

        logger.info("🔑 Extracted User Info from Auth0: Email=$rawEmail, Name=$name, Picture=$picture, Sub=$sub")

        if (rawEmail == null) {
            throw RuntimeException("Email claim not found in JWT token")
        }

        val email = EmailUtils.normalize(rawEmail)

        val existingUser = userRepository.findByEmailIgnoreCase(email)
        return if (existingUser != null) {
            logger.info("✅ User already exists: ${existingUser.email}")
            if (existingUser.referralCode.isBlank()) {
                userRepository.save(existingUser.copy(referralCode = generateReferralCode()))
            } else {
                existingUser
            }
        } else {
            try {
                val savedUser = userRepository.save(User(email = email, name = name, picture = picture))
                logger.info("✅ Created new user: ${savedUser.email}")
                savedUser
            } catch (ex: DuplicateKeyException) {
                // A concurrent request created this user first; reuse that record.
                logger.info("↩️ Concurrent create for $email — reusing existing user")
                userRepository.findByEmailIgnoreCase(email)
                    ?: throw ex
            }
        }
    }


    fun getCurrentUser(): User {
        val userId = authenticationService.getCurrentUserId()
        val user = userRepository.findById(userId).orElseThrow {
            RuntimeException("🚨 User not found.")
        }
        return promoCodeCleanupService.pruneInvalidPromoCodes(user)
    }

    fun getCurrentUserOrNull(): User? {
        val userId = authenticationService.getCurrentUserIdOrNull() ?: return null
        val user = userRepository.findById(userId).orElse(null) ?: return null
        return promoCodeCleanupService.pruneInvalidPromoCodes(user)
    }

    fun getInvitationsOfCurrentUser(): List<Invitation> {
        val userId = authenticationService.getCurrentUserId()
        return invitationRepository.findAllByOwnerId(userId)
    }

    fun getConfirmationsByInvitationId(invitationId: String): List<Confirmation> {
        val userId = authenticationService.getCurrentUserId()

        val invitation = invitationRepository.findById(invitationId).orElseThrow {
            RuntimeException("🚨 Invitation not found.")
        }

        if (invitation.ownerId != userId) {
            throw IllegalAccessException("🚨 You are not authorized to access confirmations for this invitation.")
        }

        return confirmationRepository.findAllByInvitationIdAndDeletedFalse(invitationId)
    }

    fun updateUser(updateRequest: UpdateUserRequest): Map<String, Any?> {
        val userId = authenticationService.getCurrentUserId()
        val existingUser = userRepository.findById(userId).orElseThrow {
            RuntimeException("🚨 User not found.")
        }

        val updatedFields = mutableMapOf<String, Any?>()

        val userToUpdate = existingUser.copy(
            name = updateRequest.name?.also { updatedFields["name"] = it } ?: existingUser.name,
            picture = updateRequest.picture?.also { updatedFields["picture"] = it } ?: existingUser.picture,
            phoneNumber = updateRequest.phoneNumber?.also { updatedFields["phoneNumber"] = it } ?: existingUser.phoneNumber,
            dateOfBirth = updateRequest.dateOfBirth?.also { updatedFields["dateOfBirth"] = it } ?: existingUser.dateOfBirth,
            status = updateRequest.status?.also { updatedFields["status"] = it } ?: existingUser.status,
            marketingOptIn = updateRequest.marketingOptIn?.also { updatedFields["marketingOptIn"] = it } ?: existingUser.marketingOptIn
        )

        userRepository.save(userToUpdate)
        return updatedFields
    }

    fun updateLastSeen(userId: String) {
        val user = userRepository.findById(userId).orElse(null) ?: return
        userRepository.save(user.copy(lastSeenAt = Instant.now()))
    }

    fun deleteCurrentUser() {
        val userId = authenticationService.getCurrentUserId()
        val user = userRepository.findById(userId).orElseThrow {
            RuntimeException("🚨 User not found.")
        }
        userRepository.delete(user)
    }
}
