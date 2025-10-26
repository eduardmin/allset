package com.allset.allset.dto

import com.allset.allset.model.ConfirmationStatus
import com.allset.allset.model.GuestSide

data class UpdateConfirmationRequest(
    val mainGuest: String? = null,
    val secondaryGuests: List<String>? = null,
    val status: ConfirmationStatus? = null,
    val guestSide: GuestSide? = null,
    val tableNumber: Int? = null,
    val notes: String? = null
)
