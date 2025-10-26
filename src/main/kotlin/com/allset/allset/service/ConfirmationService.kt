package com.allset.allset.service

import com.allset.allset.dto.UpdateConfirmationRequest
import com.allset.allset.model.Confirmation
import com.allset.allset.repository.ConfirmationRepository
import org.springframework.stereotype.Service

@Service
class ConfirmationService(private val confirmationRepository: ConfirmationRepository) {

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
