package com.allset.allset.controller

import com.allset.allset.dto.UpdateConfirmationRequest
import com.allset.allset.model.Confirmation
import com.allset.allset.service.ConfirmationService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/confirmations")
class ConfirmationController(
    private val confirmationService: ConfirmationService
) {

    @PostMapping
    fun createConfirmation(@RequestBody confirmation: Confirmation): Confirmation {
        return confirmationService.createConfirmation(confirmation)
    }

    @GetMapping("/invitation/{invitationId}")
    fun getConfirmationsByInvitation(@PathVariable invitationId: String): List<Confirmation> {
        return confirmationService.getConfirmationsByInvitationId(invitationId)
    }

    @PatchMapping("/{id}")
    fun updateConfirmation(
        @PathVariable id: String,
        @RequestBody updateRequest: UpdateConfirmationRequest
    ): Confirmation {
        return confirmationService.updateConfirmation(id, updateRequest)
    }

    @DeleteMapping("/{id}")
    fun deleteConfirmation(@PathVariable id: String) {
        confirmationService.deleteConfirmation(id)
    }
}
