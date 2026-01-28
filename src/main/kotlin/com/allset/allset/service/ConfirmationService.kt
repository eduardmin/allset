package com.allset.allset.service

import com.allset.allset.config.LocalizationProperties
import com.allset.allset.dto.*
import com.allset.allset.model.*
import com.allset.allset.repository.ConfirmationRepository
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class ConfirmationService(
    private val confirmationRepository: ConfirmationRepository,
    private val messageSource: MessageSource,
    private val localizationProperties: LocalizationProperties
) {

    fun getConfirmationFilters(): ConfirmationFiltersResponse {
        val filters = ConfirmationFilter.values().map { filter ->
            val key = "filter.${filter.id}"
            
            ConfirmationFilterDTO(
                id = filter.id,
                label = getLocalizedMessages(key)
            )
        }
        return ConfirmationFiltersResponse(filters)
    }

    private fun getLocalizedMessages(baseKey: String): Map<String, String> {
        return localizationProperties.supportedLanguages.associateWith { lang ->
            messageSource.getMessage(baseKey, null, Locale.forLanguageTag(lang))
        }
    }

    fun getConfirmationsByInvitationIdWithFilter(
        invitationId: String,
        filterId: String?
    ): List<Confirmation> {
        val filter = filterId?.let { ConfirmationFilter.fromId(it) } ?: ConfirmationFilter.SHOW_ALL_GUESTS
        
        return when (filter) {
            ConfirmationFilter.SHOW_ALL_GUESTS -> 
                confirmationRepository.findAllByInvitationIdAndDeletedFalse(invitationId)
            
            ConfirmationFilter.SHOW_ONLY_ADDED_BY_ME -> 
                confirmationRepository.findAllByInvitationIdAndDeletedFalseAndCreatedBy(
                    invitationId, 
                    ConfirmationCreator.INVITATION_OWNER
                )
            
            ConfirmationFilter.SHOW_DELETED_HIDDEN -> 
                confirmationRepository.findAllByInvitationIdAndDeletedTrue(invitationId)
            
            ConfirmationFilter.SHOW_ONLY_CONFIRMED -> 
                confirmationRepository.findAllByInvitationIdAndDeletedFalseAndStatus(
                    invitationId, 
                    ConfirmationStatus.CONFIRMED
                )
            
            ConfirmationFilter.SHOW_ONLY_NOT_COMING -> 
                confirmationRepository.findAllByInvitationIdAndDeletedFalseAndStatus(
                    invitationId, 
                    ConfirmationStatus.DECLINED
                )
            
            ConfirmationFilter.SHOW_ONLY_GROOM_GUESTS -> 
                confirmationRepository.findAllByInvitationIdAndDeletedFalseAndGuestSide(
                    invitationId, 
                    GuestSide.GROOM
                )
            
            ConfirmationFilter.SHOW_ONLY_BRIDE_GUESTS -> 
                confirmationRepository.findAllByInvitationIdAndDeletedFalseAndGuestSide(
                    invitationId, 
                    GuestSide.BRIDE
                )
            
            ConfirmationFilter.SHOW_GUESTS_WITHOUT_TABLE -> 
                confirmationRepository.findAllByInvitationIdAndDeletedFalseAndTableNumberIsNull(invitationId)
        }
    }

    fun createUserConfirmation(request: UserConfirmationRequest, userId: String): Confirmation {
        val confirmation = Confirmation(
            invitationId = request.invitationId,
            mainGuest = request.mainGuest,
            secondaryGuests = request.secondaryGuests,
            status = request.status,
            guestSide = request.guestSide,
            tableNumber = request.tableNumber,
            notes = request.notes,
            createdBy = ConfirmationCreator.INVITATION_OWNER
        )
        
        val sanitizedConfirmation = sanitizeGuests(confirmation)
        validateConfirmation(sanitizedConfirmation)
        return confirmationRepository.save(sanitizedConfirmation)
    }

    fun createGuestConfirmation(request: GuestConfirmationRequest): Confirmation {
        val confirmation = Confirmation(
            invitationId = request.invitationId,
            mainGuest = request.mainGuest,
            secondaryGuests = request.secondaryGuests,
            status = request.status,
            guestSide = request.guestSide,
            tableNumber = null, // Guests cannot set table numbers
            notes = request.notes,
            createdBy = ConfirmationCreator.GUEST
        )
        
        val sanitizedConfirmation = sanitizeGuests(confirmation)
        validateConfirmation(sanitizedConfirmation)
        return confirmationRepository.save(sanitizedConfirmation)
    }

    fun createConfirmation(confirmation: Confirmation): Confirmation {
        val sanitizedConfirmation = sanitizeGuests(confirmation)
        validateConfirmation(sanitizedConfirmation)
        return confirmationRepository.save(sanitizedConfirmation)
    }

    fun getConfirmationsByInvitationId(invitationId: String): List<Confirmation> {
        return confirmationRepository.findAllByInvitationIdAndDeletedFalse(invitationId)
    }

    fun deleteConfirmation(id: String) {
        val existingConfirmation = confirmationRepository.findById(id).orElseThrow {
            IllegalArgumentException("Confirmation with id $id not found")
        }

        if (!existingConfirmation.deleted) {
            confirmationRepository.save(existingConfirmation.copy(deleted = true))
        }
    }

    fun updateConfirmation(id: String, updateRequest: UpdateConfirmationRequest): Confirmation {
        val existingConfirmation = confirmationRepository.findById(id).orElseThrow {
            IllegalArgumentException("Confirmation with id $id not found")
        }

        if (existingConfirmation.deleted) {
            throw IllegalStateException("Cannot update a deleted confirmation with id $id")
        }

        val updatedConfirmation = existingConfirmation.copy(
            mainGuest = updateRequest.mainGuest ?: existingConfirmation.mainGuest,
            secondaryGuests = updateRequest.secondaryGuests ?: existingConfirmation.secondaryGuests,
            status = updateRequest.status ?: existingConfirmation.status,
            guestSide = updateRequest.guestSide ?: existingConfirmation.guestSide,
            tableNumber = updateRequest.tableNumber ?: existingConfirmation.tableNumber,
            notes = updateRequest.notes ?: existingConfirmation.notes
        )

        val sanitizedConfirmation = sanitizeGuests(updatedConfirmation)
        validateConfirmation(sanitizedConfirmation)

        return confirmationRepository.save(sanitizedConfirmation)
    }

    private fun validateConfirmation(confirmation: Confirmation) {
        if (confirmation.mainGuest.isBlank()) {
            throw IllegalArgumentException("Main guest must be provided.")
        }

        if (confirmation.tableNumber != null && confirmation.tableNumber <= 0) {
            throw IllegalArgumentException("Table number must be greater than zero if provided.")
        }
    }

    private fun sanitizeGuests(confirmation: Confirmation): Confirmation {
        val trimmedMainGuest = confirmation.mainGuest.trim()
        val cleanedSecondaryGuests = confirmation.secondaryGuests
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        return confirmation.copy(
            mainGuest = trimmedMainGuest,
            secondaryGuests = cleanedSecondaryGuests
        )
    }
}
