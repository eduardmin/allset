package com.allset.allset.service

import com.allset.allset.dto.UpdateUserRequest
import com.allset.allset.model.Invitation
import com.allset.allset.model.User
import com.allset.allset.model.Confirmation
import com.allset.allset.repository.InvitationRepository
import com.allset.allset.repository.UserRepository
import com.allset.allset.repository.ConfirmationRepository
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserService(
    private val userRepository: UserRepository,
    private val invitationRepository: InvitationRepository,
    private val confirmationRepository: ConfirmationRepository,
    private val authenticationService: AuthenticationService
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    fun saveUser(jwt: Jwt): User {
        val email = jwt.getClaim<String>("email")
        val name = jwt.getClaim<String>("name") ?: jwt.getClaim<String>("nickname") ?: "User"
        val picture = jwt.getClaim<String>("picture")
        val sub = jwt.getClaim<String>("sub")

        logger.info("🔑 Extracted User Info from Auth0: Email=$email, Name=$name, Picture=$picture, Sub=$sub")

        if (email == null) {
            throw RuntimeException("Email claim not found in JWT token")
        }

        val existingUser = userRepository.findByEmail(email)
        return if (existingUser != null) {
            logger.info("✅ User already exists: ${existingUser.email}")
            existingUser
        } else {
            val newUser = User(email = email, name = name, picture = picture)
            val savedUser = userRepository.save(newUser)
            logger.info("✅ Created new user: ${savedUser.email}")
            savedUser
        }
    }


    fun getCurrentUser(): User {
        val userId = authenticationService.getCurrentUserId()
        return userRepository.findById(userId).orElseThrow {
            RuntimeException("🚨 User not found.")
        }
    }

    fun getCurrentUserOrNull(): User? {
        val userId = authenticationService.getCurrentUserIdOrNull() ?: return null
        return userRepository.findById(userId).orElse(null)
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
            status = updateRequest.status?.also { updatedFields["status"] = it } ?: existingUser.status
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
