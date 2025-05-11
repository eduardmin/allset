package com.allset.allset.controller

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
}
