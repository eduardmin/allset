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
        val name = jwt.getClaim<String>("name")
        val picture = jwt.getClaim<String>("picture")

        logger.info("🔑 Extracted User Info: Email=$email, Name=$name, Picture=$picture")

        val existingUser = userRepository.findByEmail(email)
        return if (existingUser != null) {
            logger.info("✅ User already exists: ${existingUser.email}")
            existingUser
        } else {
            val newUser = User(email = email, name = name, picture = picture)
            userRepository.save(newUser)
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

        return confirmationRepository.findAllByInvitationId(invitationId)
    }

    fun updateUser(updateRequest: UpdateUserRequest): User {
        val userId = authenticationService.getCurrentUserId()
        val existingUser = userRepository.findById(userId).orElseThrow {
            RuntimeException("🚨 User not found.")
        }

        val userToUpdate = existingUser.copy(
            name = updateRequest.name ?: existingUser.name,
            picture = if (updateRequest.picture != null) updateRequest.picture else existingUser.picture,
            phoneNumber = if (updateRequest.phoneNumber != null) updateRequest.phoneNumber else existingUser.phoneNumber,
            dateOfBirth = if (updateRequest.dateOfBirth != null) updateRequest.dateOfBirth else existingUser.dateOfBirth
        )

        return userRepository.save(userToUpdate)
    }

    fun deleteCurrentUser() {
        val userId = authenticationService.getCurrentUserId()
        val user = userRepository.findById(userId).orElseThrow {
            RuntimeException("🚨 User not found.")
        }
        userRepository.delete(user)
    }
}
