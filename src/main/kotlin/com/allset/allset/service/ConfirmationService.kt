package com.allset.allset.service

import com.allset.allset.model.Confirmation
import com.allset.allset.model.ConfirmationStatus
import com.allset.allset.repository.ConfirmationRepository
import org.springframework.stereotype.Service

@Service
class ConfirmationService(private val confirmationRepository: ConfirmationRepository) {

    fun createConfirmation(confirmation: Confirmation): Confirmation {
        validateConfirmation(confirmation)
        return confirmationRepository.save(confirmation)
    }

    fun getConfirmationsByInvitationId(invitationId: String): List<Confirmation> {
        return confirmationRepository.findAllByInvitationId(invitationId)
    }

    fun deleteConfirmation(id: String) {
        confirmationRepository.deleteById(id)
    }

    private fun validateConfirmation(confirmation: Confirmation) {
        when (confirmation.status) {
            ConfirmationStatus.CONFIRMED -> {
                if (confirmation.guestNames.isEmpty() || confirmation.guestNames.size > 3) {
                    throw IllegalArgumentException("If confirmed, you must provide 1 to 3 guest names.")
                }
                if (confirmation.guestCount != confirmation.guestNames.size) {
                    throw IllegalArgumentException("Guest count must match number of guest names.")
                }
            }
            ConfirmationStatus.DECLINED -> {
                if (confirmation.guestNames.size != 1) {
                    throw IllegalArgumentException("If declined, you must provide exactly 1 guest name.")
                }
                if (confirmation.guestCount != 1) {
                    throw IllegalArgumentException("Guest count must be 1 if declined.")
                }
            }
        }
    }
}
