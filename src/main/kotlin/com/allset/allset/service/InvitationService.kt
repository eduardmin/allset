package com.allset.allset.service

import com.allset.allset.dto.PartialInvitationDTO
import com.allset.allset.dto.mergeWithPartialUpdate
import com.allset.allset.model.Invitation
import com.allset.allset.repository.InvitationRepository
import com.allset.allset.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class InvitationService(
    private val invitationRepository: InvitationRepository,
    private val userRepository: UserRepository,
    private val authenticationService: AuthenticationService,
    private val templateService: TemplateService,
    private val invitationDefaultsService: InvitationDefaultsService
) {

    fun createInvitation(invitation: Invitation): Invitation {
        val userId = authenticationService.getCurrentUserId()

        userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("User with ID $userId not found.")
        }

        val validTemplateIds = templateService.getTemplates().map { it.id }
        if (invitation.templateId !in validTemplateIds) {
            throw IllegalArgumentException("Invalid templateId: ${invitation.templateId}")
        }

        // Apply defaults for missing fields
        val invitationWithDefaults = applyDefaults(invitation)

        val invitationToSave = invitationWithDefaults.copy(
            id = null,
            ownerId = userId
        )
        return invitationRepository.save(invitationToSave)
    }

    private fun applyDefaults(invitation: Invitation): Invitation {
        // Check if description is empty or contains only empty values
        val description = if (invitation.description.values.all { it.isBlank() }) {
            invitationDefaultsService.getDefaultDescription()
        } else {
            invitation.description
        }

        // Apply default dress code if missing entirely OR if description is empty
        val dressCode = when {
            // If dressCode is null, don't create a default one (user didn't want it)
            invitation.dressCode == null -> null
            // If dressCode exists but description is empty, fill with defaults
            invitation.dressCode.description.values.all { it.isBlank() } -> {
                invitation.dressCode.copy(
                    description = invitationDefaultsService.getDefaultDressCodeDescription()
                )
            }
            // Otherwise keep the user's description
            else -> invitation.dressCode
        }

        // Apply default our story text if missing or empty
        val ourStory = when {
            // If ourStory is null, don't create a default one
            invitation.ourStory == null -> null
            // If ourStory exists but text is empty, fill with defaults
            invitation.ourStory.text.values.all { it.isBlank() } -> {
                invitation.ourStory.copy(
                    text = invitationDefaultsService.getDefaultOurStoryText()
                )
            }
            // Otherwise keep the user's text
            else -> invitation.ourStory
        }

        return invitation.copy(
            description = description,
            dressCode = dressCode,
            ourStory = ourStory
        )
    }

    fun getInvitations(): List<Invitation> {
        val userId = authenticationService.getCurrentUserId()
        return invitationRepository.findAllByOwnerId(userId)
    }

    fun getInvitationById(id: String): Invitation? {
        return invitationRepository.findById(id).orElse(null)
    }

    fun getInvitationByUrlExtension(urlExtension: String): Invitation? {
        return invitationRepository.findByUrlExtension(urlExtension)
    }

    fun updateInvitation(id: String, updatedInvitation: Invitation): Invitation? {
        val userId = authenticationService.getCurrentUserId()
        val existingInvitation = invitationRepository.findById(id).orElse(null)

        return if (existingInvitation != null && existingInvitation.ownerId == userId) {
            val invitationWithDefaults = applyDefaults(updatedInvitation)
            invitationRepository.save(invitationWithDefaults.copy(id = id, ownerId = userId))
        } else {
            null
        }
    }

    fun patchInvitation(id: String, patch: PartialInvitationDTO): Invitation {
        val userId = authenticationService.getCurrentUserId()
        val existing = invitationRepository.findById(id).orElseThrow {
            IllegalArgumentException("Invitation with id $id not found.")
        }

        if (existing.ownerId != userId) {
            throw IllegalAccessException("Unauthorized to modify this invitation.")
        }

        val merged = existing.mergeWithPartialUpdate(patch)
        return invitationRepository.save(merged)
    }

    fun deleteInvitation(id: String) {
        val userId = authenticationService.getCurrentUserId()
        val invitation = invitationRepository.findById(id).orElse(null)

        if (invitation != null && invitation.ownerId == userId) {
            invitationRepository.deleteById(id)
        }
    }
}
