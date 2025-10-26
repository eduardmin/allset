package com.allset.allset.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "confirmations")
data class Confirmation(
    @Id val id: String? = null,
    val invitationId: String,
    val mainGuest: String,
    val secondaryGuests: List<String> = emptyList(),
    val status: ConfirmationStatus,
    val guestSide: GuestSide,
    val tableNumber: Int? = null,
    val notes: String? = null,
    val createdBy: ConfirmationCreator,
    val deleted: Boolean = false,
    val createdAt: Instant = Instant.now()
)

enum class ConfirmationStatus {
    CONFIRMED,
    DECLINED
}

enum class GuestSide {
    GROOM,
    BRIDE
}

enum class ConfirmationCreator {
    INVITATION_OWNER,
    GUEST
}
