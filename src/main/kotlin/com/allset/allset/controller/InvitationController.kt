package com.allset.allset.controller

import com.allset.allset.dto.InvitationDTO
import com.allset.allset.dto.PartialInvitationDTO
import com.allset.allset.dto.toDTO
import com.allset.allset.dto.toEntity
import com.allset.allset.service.AuthenticationService
import com.allset.allset.service.InvitationService
import com.allset.allset.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/invitations")
class InvitationController(
    private val invitationService: InvitationService, 
    private val authenticationService: AuthenticationService, 
    private val userService: UserService
) {

    // Create new draft
    @PostMapping("/draft")
    fun createDraft(@RequestBody invitationDTO: InvitationDTO): InvitationDTO {
        val userId = authenticationService.getCurrentUserId()
        val invitation = invitationDTO.toEntity(userId)
        return invitationService.saveDraft(invitation).toDTO()
    }

    // Update existing draft (full update)
    @PutMapping("/draft/{id}")
    fun updateDraft(@PathVariable id: String, @RequestBody invitationDTO: InvitationDTO): InvitationDTO {
        val userId = authenticationService.getCurrentUserId()
        val invitation = invitationDTO.toEntity(userId)
        return invitationService.updateDraft(id, invitation).toDTO()
    }

    // Partial update draft (for auto-save)
    @PatchMapping("/draft/{id}")
    fun patchDraft(@PathVariable id: String, @RequestBody patch: PartialInvitationDTO): InvitationDTO {
        return invitationService.patchDraft(id, patch).toDTO()
    }

    // Delete draft
    @DeleteMapping("/draft/{id}")
    fun deleteDraft(@PathVariable id: String) {
        invitationService.deleteDraft(id)
    }

    // Get all drafts
    @GetMapping("/drafts")
    fun getDrafts(): List<InvitationDTO> {
        return invitationService.getDrafts().map { it.toDTO() }
    }

    // Get active invitations
    @GetMapping("/active")
    fun getActiveInvitations(): List<InvitationDTO> {
        return invitationService.getActiveInvitations().map { it.toDTO() }
    }

    // Get expired invitations
    @GetMapping("/expired")
    fun getExpiredInvitations(): List<InvitationDTO> {
        return invitationService.getExpiredInvitations().map { it.toDTO() }
    }

    // Publish a draft
    @PostMapping("/{id}/publish")
    fun publishDraft(@PathVariable id: String): InvitationDTO {
        return invitationService.publishDraft(id).toDTO()
    }

    @PostMapping
    fun createInvitation(@RequestBody invitationDTO: InvitationDTO): InvitationDTO {
        val userId = authenticationService.getCurrentUserId()
        val user = userService.getCurrentUser()

//        if (!user.isPaid) {
//            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You must complete payment to create an invitation.")
//        }

        val invitation = invitationDTO.toEntity(userId)
        return invitationService.createInvitation(invitation).toDTO()
    }

    @GetMapping
    fun getInvitations(): List<InvitationDTO> {
        return invitationService.getInvitations().map { it.toDTO() }
    }

    @GetMapping("/{id}")
    fun getInvitationById(@PathVariable id: String): InvitationDTO? {
        return invitationService.getInvitationById(id)?.toDTO()
    }

    @PutMapping("/{id}")
    fun updateInvitation(@PathVariable id: String, @RequestBody invitationDTO: InvitationDTO): InvitationDTO? {
        val userId = authenticationService.getCurrentUserId()
        val invitation = invitationDTO.toEntity(userId)
        return invitationService.updateInvitation(id, invitation)?.toDTO()
    }

    @PatchMapping("/{id}")
    fun patchInvitation(@PathVariable id: String, @RequestBody patch: PartialInvitationDTO): InvitationDTO {
        return invitationService.patchInvitation(id, patch).toDTO()
    }


    @DeleteMapping("/{id}")
    fun deleteInvitation(@PathVariable id: String) {
        invitationService.deleteInvitation(id)
    }

    @GetMapping("/url/{url}")
    fun getInvitationByUrlExtension(@PathVariable url: String): InvitationDTO? {
        return invitationService.getInvitationByUrlExtension(url)?.toDTO()
    }
}
