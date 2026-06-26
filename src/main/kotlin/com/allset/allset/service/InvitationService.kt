package com.allset.allset.service

import com.allset.allset.dto.PartialInvitationDTO
import com.allset.allset.dto.mergeWithPartialUpdate
import com.allset.allset.model.Invitation
import com.allset.allset.model.InvitationStatus
import com.allset.allset.model.TemplateType
import com.allset.allset.repository.ConfirmationRepository
import com.allset.allset.repository.InvitationRepository
import com.allset.allset.repository.UserRepository
import com.allset.allset.util.InvitationSlug
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class InvitationService(
    private val invitationRepository: InvitationRepository,
    private val userRepository: UserRepository,
    private val authenticationService: AuthenticationService,
    private val templateService: TemplateService,
    private val invitationDefaultsService: InvitationDefaultsService,
    private val pricingService: PricingService,
    private val confirmationRepository: ConfirmationRepository
) {

    fun generateUniqueUrl(title: Map<String, String>): String {
        val base = InvitationSlug.baseSlugSegment(title)

        if (invitationRepository.findAllByUrlExtension(base).isEmpty()) return base

        var counter = 1
        while (true) {
            val candidate = "$base-$counter"
            if (invitationRepository.findAllByUrlExtension(candidate).isEmpty()) return candidate
            counter++
        }
    }

    // Create new draft
    fun saveDraft(invitation: Invitation): Invitation {
        val userId = authenticationService.getCurrentUserId()

        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("User with ID $userId not found.")
        }

        val invitationWithDefaults = applyDefaults(invitation)

        val templateBasePrice = templateService.getBasePriceForTemplate(invitation.templateId)
        val pricingSummary = pricingService.summarize(user.appliedPromoCodes, templateBasePrice)

        val invitationToSave = invitationWithDefaults.copy(
            id = null,
            ownerId = userId,
            title = formatTitleForTemplate(invitationWithDefaults.title, invitationWithDefaults.templateId),
            urlExtension = generateUniqueUrl(invitation.title),
            status = InvitationStatus.DRAFT,
            pricing = pricingSummary,
            createdAt = Instant.now(),
            lastModifiedAt = Instant.now()
        )

        return invitationRepository.save(invitationToSave)
    }

    // Partial update draft (for auto-save)
    fun patchDraft(id: String, patch: PartialInvitationDTO): Invitation {
        val userId = authenticationService.getCurrentUserId()
        
        val existingDraft = invitationRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Draft with id $id not found")
        }

        if (existingDraft.ownerId != userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to update this draft")
        }

        if (existingDraft.status != InvitationStatus.DRAFT) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Can only update drafts, not published invitations")
        }

        val merged = existingDraft.mergeWithPartialUpdate(patch)

        val updatedUrl = if (patch.title != null) generateUniqueUrl(patch.title) else existingDraft.urlExtension

        return invitationRepository.save(merged.copy(
            title = formatTitleForTemplate(merged.title, merged.templateId),
            urlExtension = updatedUrl,
            lastModifiedAt = Instant.now()
        ))
    }

    // Delete draft
    fun deleteDraft(id: String) {
        val userId = authenticationService.getCurrentUserId()
        
        val draft = invitationRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Draft with id $id not found")
        }

        if (draft.ownerId != userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to delete this draft")
        }

        if (draft.status != InvitationStatus.DRAFT) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Can only delete drafts, not published invitations")
        }

        invitationRepository.deleteById(id)
    }

    fun getDrafts(): List<Invitation> {
        val userId = authenticationService.getCurrentUserId()
        return invitationRepository.findAllByOwnerIdAndStatus(userId, InvitationStatus.DRAFT)
    }

    fun deleteAllDrafts() {
        val userId = authenticationService.getCurrentUserId()
        val drafts = invitationRepository.findAllByOwnerIdAndStatus(userId, InvitationStatus.DRAFT)
        invitationRepository.deleteAll(drafts)
    }

    // Delete all draft and active invitations owned by the current user.
    fun deleteAllUserInvitations(): Int {
        val userId = authenticationService.getCurrentUserId()
        val toDelete = invitationRepository.findAllByOwnerId(userId)
            .filter { it.status == InvitationStatus.DRAFT || it.status == InvitationStatus.ACTIVE }
        if (toDelete.isNotEmpty()) {
            invitationRepository.deleteAll(toDelete)
        }
        return toDelete.size
    }

    // Get active invitations (published)
    fun getActiveInvitations(): List<Invitation> {
        val userId = authenticationService.getCurrentUserId()
        return invitationRepository.findAllByOwnerIdAndStatus(userId, InvitationStatus.ACTIVE)
    }
    
    // Get guest count for an invitation
    fun getGuestCount(invitationId: String): Int {
        return confirmationRepository.findAllByInvitationIdAndDeletedFalse(invitationId).size
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
            ResponseStatusException(HttpStatus.NOT_FOUND, "Draft with id $id not found")
        }

        if (draft.ownerId != userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to publish this draft")
        }

        if (draft.status != InvitationStatus.DRAFT) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Only drafts can be published")
        }

        // Always derive slug from current title so publish works after title-only edits (e.g. hy-only)
        // and never fails validation for a stale or empty stored urlExtension.
        val draftReady = draft.copy(urlExtension = generateUniqueUrl(draft.title))

        // Validate required fields before publishing
        validateForPublishing(draftReady)

        val validTemplateIds = templateService.getTemplates().map { it.id }
        if (draftReady.templateId !in validTemplateIds) {
            throw IllegalArgumentException("Invalid templateId: ${draftReady.templateId}")
        }

        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("User with ID $userId not found.")
        }

        val templateBasePrice = templateService.getBasePriceForTemplate(draftReady.templateId)
        val pricingSummary = pricingService.summarize(user.appliedPromoCodes, templateBasePrice)

        val publishedAt = Instant.now()
        val expiresAt = publishedAt.plus(365, ChronoUnit.DAYS)

        val publishedInvitation = draftReady.copy(
            status = InvitationStatus.ACTIVE,
            publishedAt = publishedAt,
            expiresAt = expiresAt,
            pricing = pricingSummary,
            lastModifiedAt = Instant.now()
        )

        return invitationRepository.save(publishedInvitation)
    }

    fun publishDraftAfterPayment(invitationId: String, userId: String): Invitation {
        val draft = invitationRepository.findById(invitationId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Draft with id $invitationId not found")
        }

        if (draft.ownerId != userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Payment user does not own this draft")
        }

        if (draft.status != InvitationStatus.DRAFT) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Only drafts can be published")
        }

        val draftReady = draft.copy(urlExtension = generateUniqueUrl(draft.title))
        validateForPublishing(draftReady)

        val validTemplateIds = templateService.getTemplates().map { it.id }
        if (draftReady.templateId !in validTemplateIds) {
            throw IllegalArgumentException("Invalid templateId: ${draftReady.templateId}")
        }

        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("User with ID $userId not found.")
        }

        val templateBasePrice = templateService.getBasePriceForTemplate(draftReady.templateId)
        val pricingSummary = pricingService.summarize(user.appliedPromoCodes, templateBasePrice)

        val publishedAt = Instant.now()
        val expiresAt = publishedAt.plus(365, ChronoUnit.DAYS)

        val publishedInvitation = draftReady.copy(
            status = InvitationStatus.ACTIVE,
            publishedAt = publishedAt,
            expiresAt = expiresAt,
            pricing = pricingSummary,
            lastModifiedAt = Instant.now()
        )

        return invitationRepository.save(publishedInvitation)
    }

    fun validateForPayment(invitationId: String): Invitation {
        val userId = authenticationService.getCurrentUserId()
        val invitation = invitationRepository.findById(invitationId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found")
        }

        if (invitation.ownerId != userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Not your invitation")
        }

        if (invitation.status != InvitationStatus.DRAFT) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitation is already ${invitation.status}")
        }

        val invitationReady = invitation.copy(urlExtension = generateUniqueUrl(invitation.title))
        validateForPublishing(invitationReady)
        return invitationReady
    }

    private fun validateForPublishing(invitation: Invitation) {
        if (invitation.title.values.all { it.isBlank() }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required to publish")
        }
        if (invitation.urlExtension.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "URL extension is required to publish")
        }
        if (invitation.eventDate == null || invitation.eventDate.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Event date is required to publish")
        }
        if (invitation.mainImages == null || invitation.mainImages.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one main image is required to publish")
        }
    }

    fun createInvitation(invitation: Invitation): Invitation {
        val userId = authenticationService.getCurrentUserId()

        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("User with ID $userId not found.")
        }

        val validTemplateIds = templateService.getTemplates().map { it.id }
        if (invitation.templateId !in validTemplateIds) {
            throw IllegalArgumentException("Invalid templateId: ${invitation.templateId}")
        }

        // Apply defaults for missing fields
        val invitationWithDefaults = applyDefaults(invitation)

        val templateBasePrice = templateService.getBasePriceForTemplate(invitation.templateId)
        val pricingSummary = pricingService.summarize(user.appliedPromoCodes, templateBasePrice)

        val publishedAt = Instant.now()
        val expiresAt = publishedAt.plus(365, ChronoUnit.DAYS)

        val invitationToSave = invitationWithDefaults.copy(
            id = null,
            ownerId = userId,
            title = formatTitleForTemplate(invitationWithDefaults.title, invitationWithDefaults.templateId),
            urlExtension = generateUniqueUrl(invitation.title),
            status = InvitationStatus.ACTIVE,
            publishedAt = publishedAt,
            expiresAt = expiresAt,
            pricing = pricingSummary
        )
        return invitationRepository.save(invitationToSave)
    }

    // Formats a comma-separated couple title (e.g. "Eduard, Lia") into the template-specific
    // joined form: Rustic Love Story uses "Eduard + Lia"; all other templates use "Eduard & Lia".
    // Idempotent: a title that no longer contains a comma is returned trimmed and unchanged.
    private fun formatTitleForTemplate(title: Map<String, String>, templateId: String): Map<String, String> {
        val type = templateService.getTemplateType(templateId)
        val separator = if (type == TemplateType.Rustic_Love_Story) " + " else " & "
        return title.mapValues { (_, value) ->
            val parts = value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (parts.size >= 2) parts.joinToString(separator) else value.trim()
        }
    }

    private fun applyDefaults(invitation: Invitation): Invitation {
        val tId = invitation.templateId

        val description = if (invitation.description == null || invitation.description.values.all { it.isBlank() }) {
            invitationDefaultsService.getDefaultDescription(tId)
        } else {
            invitation.description
        }

        val dressCode = when {
            invitation.dressCode == null -> null
            invitation.dressCode.description.values.all { it.isBlank() } -> {
                invitation.dressCode.copy(
                    description = invitationDefaultsService.getDefaultDressCodeDescription(tId)
                )
            }
            else -> invitation.dressCode
        }

        val ourStory = when {
            invitation.ourStory == null -> null
            invitation.ourStory.text.values.all { it.isBlank() } -> {
                invitation.ourStory.copy(
                    text = invitationDefaultsService.getDefaultOurStoryText(tId)
                )
            }
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
        val results = invitationRepository.findAllByUrlExtension(urlExtension)
        return results.firstOrNull { it.status == InvitationStatus.ACTIVE }
            ?: results.firstOrNull()
    }

    fun updateInvitation(id: String, updatedInvitation: Invitation): Invitation? {
        val userId = authenticationService.getCurrentUserId()
        val existingInvitation = invitationRepository.findById(id).orElse(null)

        return if (existingInvitation != null && existingInvitation.ownerId == userId) {
            val user = userRepository.findById(userId).orElseThrow {
                IllegalArgumentException("User with ID $userId not found.")
            }
            val invitationWithDefaults = applyDefaults(updatedInvitation)
            val templateBasePrice = templateService.getBasePriceForTemplate(updatedInvitation.templateId)
            val pricingSummary = pricingService.summarize(user.appliedPromoCodes, templateBasePrice)

            invitationRepository.save(invitationWithDefaults.copy(
                id = id,
                ownerId = userId,
                title = formatTitleForTemplate(invitationWithDefaults.title, invitationWithDefaults.templateId),
                pricing = pricingSummary,
                lastModifiedAt = Instant.now()
            ))
        } else {
            null
        }
    }

    fun patchInvitation(id: String, patch: PartialInvitationDTO): Invitation {
        val userId = authenticationService.getCurrentUserId()
        val existing = invitationRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation with id $id not found")
        }

        if (existing.ownerId != userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to modify this invitation")
        }

        val mergedBase = existing.mergeWithPartialUpdate(patch)
        val merged = mergedBase.copy(
            title = formatTitleForTemplate(mergedBase.title, mergedBase.templateId),
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
