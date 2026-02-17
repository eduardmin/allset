package com.allset.allset.controller

import com.allset.allset.dto.UpdateUserRequest
import com.allset.allset.dto.UserDTO
import com.allset.allset.dto.toDTO
import com.allset.allset.model.Confirmation
import com.allset.allset.model.Invitation
import com.allset.allset.service.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService) {

    @GetMapping
    fun getCurrentUser(): UserDTO {
        return userService.getCurrentUser().toDTO()
    }

    @PatchMapping
    fun updateCurrentUser(@RequestBody updateUserRequest: UpdateUserRequest): UserDTO {
        return userService.updateUser(updateUserRequest).toDTO()
    }

    @GetMapping("/invitations")
    fun getMyInvitations(): List<Invitation> {
        return userService.getInvitationsOfCurrentUser()
    }

    @GetMapping("/invitations/{invitationId}/confirmations")
    fun getInvitationConfirmations(@PathVariable invitationId: String): List<Confirmation> {
        return userService.getConfirmationsByInvitationId(invitationId)
    }

    @DeleteMapping
    fun deleteCurrentUser() {
        userService.deleteCurrentUser()
    }
}


