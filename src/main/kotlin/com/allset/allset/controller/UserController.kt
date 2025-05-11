package com.allset.allset.controller

import com.allset.allset.model.Confirmation
import com.allset.allset.model.Invitation
import com.allset.allset.model.User
import com.allset.allset.service.UserService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {

    @GetMapping("/")
    fun getCurrentUser(): User {
        return userService.getCurrentUser()
    }

    @GetMapping("/invitations")
    fun getMyInvitations(): List<Invitation> {
        return userService.getInvitationsOfCurrentUser()
    }

    @GetMapping("/invitations/{invitationId}/confirmations")
    fun getInvitationConfirmations(@PathVariable invitationId: String): List<Confirmation> {
        return userService.getConfirmationsByInvitationId(invitationId)
    }

    @DeleteMapping("/")
    fun deleteCurrentUser() {
        userService.deleteCurrentUser()
    }
}

