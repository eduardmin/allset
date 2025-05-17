package com.allset.allset.controller

import com.allset.allset.dto.InvitationDTO
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
class InvitationController(private val invitationService: InvitationService, private val authenticationService: AuthenticationService, private val userService: UserService
) {

    @PostMapping
    fun createInvitation(@RequestBody invitationDTO: InvitationDTO): InvitationDTO {
        val userId = authenticationService.getCurrentUserId()
        val user = userService.getCurrentUser()

        if (!user.isPaid) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You must complete payment to create an invitation.")
        }

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

    @DeleteMapping("/{id}")
    fun deleteInvitation(@PathVariable id: String) {
        invitationService.deleteInvitation(id)
    }

    @GetMapping("/{url}")
    fun getInvitationByUrlExtension(@PathVariable url: String): InvitationDTO? {
        return invitationService.getInvitationByUrlExtension(url)?.toDTO()
    }
}
