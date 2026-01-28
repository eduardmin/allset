package com.allset.allset.controller

import com.allset.allset.dto.*
import com.allset.allset.model.Confirmation
import com.allset.allset.service.ConfirmationService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/confirmations")
class ConfirmationController(
    private val confirmationService: ConfirmationService
) {

    @GetMapping("/filters")
    fun getConfirmationFilters(): ConfirmationFiltersResponse {
        return confirmationService.getConfirmationFilters()
    }

    @PostMapping("/user")
    fun createUserConfirmation(
        @RequestBody request: UserConfirmationRequest,
        authentication: Authentication
    ): Confirmation {
        val userId = authentication.name // Get user ID from JWT token
        return confirmationService.createUserConfirmation(request, userId)
    }

    // For guests (publicly accessible)
    @PostMapping("/guest")
    fun createGuestConfirmation(
        @RequestBody request: GuestConfirmationRequest
    ): Confirmation {
        return confirmationService.createGuestConfirmation(request)
    }

    @PostMapping
    fun createConfirmation(@RequestBody confirmation: Confirmation): Confirmation {
        return confirmationService.createConfirmation(confirmation)
    }

    @GetMapping("/invitation/{invitationId}")
    fun getConfirmationsByInvitation(
        @PathVariable invitationId: String,
        @RequestParam(required = false) filterId: String?
    ): List<Confirmation> {
        return confirmationService.getConfirmationsByInvitationIdWithFilter(invitationId, filterId)
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
