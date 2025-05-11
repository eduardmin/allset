package com.allset.allset.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "confirmations")
data class Confirmation(
    @Id val id: String? = null,
    val invitationId: String,
    val guestNames: List<String>,
    val guestCount: Int,
    val status: ConfirmationStatus,
    val notes: String? = null,
    val createdAt: Instant = Instant.now()
)

enum class ConfirmationStatus {
    CONFIRMED,
    DECLINED
}
