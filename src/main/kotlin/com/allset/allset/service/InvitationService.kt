package com.allset.allset.service

import com.allset.allset.dto.PartialInvitationDTO
import com.allset.allset.dto.mergeWithPartialUpdate
import com.allset.allset.model.Invitation
import com.allset.allset.model.InvitationStatus
import com.allset.allset.repository.InvitationRepository
import com.allset.allset.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class InvitationService(
    private val invitationRepository: InvitationRepository,
    private val userRepository: UserRepository,
    private val authenticationService: AuthenticationService,
    private val templateService: TemplateService,
    private val invitationDefaultsService: InvitationDefaultsService
) {

    // Create new draft
    fun saveDraft(invitation: Invitation): Invitation {
        val userId = authenticationService.getCurrentUserId()

        userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("User with ID $userId not found.")
        }

        // Apply defaults for missing fields
        val invitationWithDefaults = applyDefaults(invitation)

        val invitationToSave = invitationWithDefaults.copy(
            id = null, // Ensure new document
            ownerId = userId,
            status = InvitationStatus.DRAFT,
            createdAt = Instant.now(),
            lastModifiedAt = Instant.now()
        )
        
        return invitationRepository.save(invitationToSave)
    }

    // Update existing draft (full update)
    fun updateDraft(id: String, updatedInvitation: Invitation): Invitation {
        val userId = authenticationService.getCurrentUserId()
        
        val existingDraft = invitationRepository.findById(id).orElseThrow {
            IllegalArgumentException("Draft with id $id not found")
        }

        if (existingDraft.ownerId != userId) {
            throw IllegalAccessException("Unauthorized to update this draft")
        }

        if (existingDraft.status != InvitationStatus.DRAFT) {
            throw IllegalStateException("Can only update drafts, not published invitations")
        }

        // Apply defaults for missing fields
        val invitationWithDefaults = applyDefaults(updatedInvitation)

        val invitationToSave = invitationWithDefaults.copy(
            id = id,
            ownerId = userId,
            status = InvitationStatus.DRAFT,
            createdAt = existingDraft.createdAt, // Keep original creation time
            lastModifiedAt = Instant.now()
        )
        
        return invitationRepository.save(invitationToSave)
    }

    // Partial update draft (for auto-save)
    fun patchDraft(id: String, patch: PartialInvitationDTO): Invitation {
        val userId = authenticationService.getCurrentUserId()
        
        val existingDraft = invitationRepository.findById(id).orElseThrow {
            IllegalArgumentException("Draft with id $id not found")
        }

        if (existingDraft.ownerId != userId) {
            throw IllegalAccessException("Unauthorized to update this draft")
        }

        if (existingDraft.status != InvitationStatus.DRAFT) {
            throw IllegalStateException("Can only update drafts, not published invitations")
        }

        val merged = existingDraft.mergeWithPartialUpdate(patch).copy(
            lastModifiedAt = Instant.now()
        )
        
        return invitationRepository.save(merged)
    }

    // Delete draft
    fun deleteDraft(id: String) {
        val userId = authenticationService.getCurrentUserId()
        
        val draft = invitationRepository.findById(id).orElseThrow {
            IllegalArgumentException("Draft with id $id not found")
        }

        if (draft.ownerId != userId) {
            throw IllegalAccessException("Unauthorized to delete this draft")
        }

        if (draft.status != InvitationStatus.DRAFT) {
            throw IllegalStateException("Can only delete drafts, not published invitations")
        }

        invitationRepository.deleteById(id)
    }

    // Get all drafts for current user
    fun getDrafts(): List<Invitation> {
        val userId = authenticationService.getCurrentUserId()
        return invitationRepository.findAllByOwnerIdAndStatus(userId, InvitationStatus.DRAFT)
    }

    // Get active invitations (published)
    fun getActiveInvitations(): List<Invitation> {
        val userId = authenticationService.getCurrentUserId()
        return invitationRepository.findAllByOwnerIdAndStatus(userId, InvitationStatus.ACTIVE)
    }

    // Get expired invitations
    fun getExpiredInvitations(): List<Invitation> {
        val userId = authenticationService.getCurrentUserId()
        return invitationRepository.findAllByOwnerIdAndStatus(userId, InvitationStatus.EXPIRED)
    }

    // Publish draft (converts draft to active)
    fun publishDraft(id: String): Invitation {
        val userId = authenticationService.getCurrentUserId()
        val draft = invitationRepository.findById(id).orElseThrow {
            IllegalArgumentException("Draft with id $id not found")
        }

        if (draft.ownerId != userId) {
            throw IllegalAccessException("Unauthorized to publish this draft")
        }

        if (draft.status != InvitationStatus.DRAFT) {
            throw IllegalStateException("Only drafts can be published")
        }

        // Validate required fields before publishing
        validateForPublishing(draft)

        val validTemplateIds = templateService.getTemplates().map { it.id }
        if (draft.templateId !in validTemplateIds) {
            throw IllegalArgumentException("Invalid templateId: ${draft.templateId}")
        }

        val publishedInvitation = draft.copy(
            status = InvitationStatus.ACTIVE,
            publishedAt = Instant.now(),
            lastModifiedAt = Instant.now()
        )

        return invitationRepository.save(publishedInvitation)
    }

    private fun validateForPublishing(invitation: Invitation) {
        if (invitation.title.values.all { it.isBlank() }) {
            throw IllegalArgumentException("Title is required to publish")
        }
        if (invitation.urlExtension.isBlank()) {
            throw IllegalArgumentException("URL extension is required to publish")
        }
        if (invitation.eventDate == null || invitation.eventDate.isBlank()) {
            throw IllegalArgumentException("Event date is required to publish")
        }
        if (invitation.mainImages == null || invitation.mainImages.isEmpty()) {
            throw IllegalArgumentException("At least one main image is required to publish")
        }
    }

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
            ownerId = userId,
            status = InvitationStatus.ACTIVE,
            publishedAt = Instant.now()
        )
        return invitationRepository.save(invitationToSave)
    }

    private fun applyDefaults(invitation: Invitation): Invitation {
        // Check if description is null or empty
        val description = if (invitation.description == null || invitation.description.values.all { it.isBlank() }) {
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
            invitationRepository.save(invitationWithDefaults.copy(
                id = id, 
                ownerId = userId,
                lastModifiedAt = Instant.now()
            ))
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

        val merged = existing.mergeWithPartialUpdate(patch).copy(
            lastModifiedAt = Instant.now()
        )
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
