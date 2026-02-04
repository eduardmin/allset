package com.allset.allset.dto

import com.allset.allset.model.ConfirmationStatus
import com.allset.allset.model.GuestSide

data class GuestConfirmationRequest(
    val invitationId: String,
    val mainGuest: String,
    val secondaryGuests: List<String> = emptyList(),
    val status: ConfirmationStatus,
    val guestSide: GuestSide,
    val notes: String? = null
)


