package com.allset.allset.dto

import com.allset.allset.model.InvitationStatus
import java.time.Instant

data class AdminInvitationSummary(
    val id: String?,
    val title: Map<String, String>,
    val templateId: String,
    val templateName: Map<String, String>,
    val status: InvitationStatus,
    val createdAt: Instant,
    val publishedAt: Instant?,
    val expiresAt: Instant?
)
