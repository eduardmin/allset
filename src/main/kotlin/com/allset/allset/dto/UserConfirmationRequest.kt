package com.allset.allset.dto

import com.allset.allset.model.ConfirmationStatus
import com.allset.allset.model.GuestSide

data class UserConfirmationRequest(
    val invitationId: String,
    val mainGuest: String,
    val secondaryGuests: List<String> = emptyList(),
    val status: ConfirmationStatus,
    val guestSide: GuestSide,
    val tableNumber: Int? = null,
    val notes: String? = null
)


