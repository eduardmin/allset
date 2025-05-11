package com.allset.allset.service

import com.allset.allset.model.Invitation
import com.allset.allset.repository.InvitationRepository
import com.allset.allset.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class InvitationService(
    private val invitationRepository: InvitationRepository,
    private val userRepository: UserRepository,
    private val authenticationService: AuthenticationService
) {

    fun createInvitation(invitation: Invitation): Invitation {
        val userId = authenticationService.getCurrentUserId()

        userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("User with ID $userId not found.")
        }

        val invitationToSave = invitation.copy(
            id = null,
            ownerId = userId
        )
        return invitationRepository.save(invitationToSave)
    }

    fun getInvitations(): List<Invitation> {
        val userId = authenticationService.getCurrentUserId()
        return invitationRepository.findAllByOwnerId(userId)
    }

    fun getInvitationById(id: String): Invitation? {
        return invitationRepository.findById(id).orElse(null)
    }

    fun updateInvitation(id: String, updatedInvitation: Invitation): Invitation? {
        val userId = authenticationService.getCurrentUserId()
        val existingInvitation = invitationRepository.findById(id).orElse(null)

        return if (existingInvitation != null && existingInvitation.ownerId == userId) {
            invitationRepository.save(updatedInvitation.copy(id = id, ownerId = userId))
        } else {
            null
        }
    }

    fun deleteInvitation(id: String) {
        val userId = authenticationService.getCurrentUserId()
        val invitation = invitationRepository.findById(id).orElse(null)

        if (invitation != null && invitation.ownerId == userId) {
            invitationRepository.deleteById(id)
        }
    }
}
