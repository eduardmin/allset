package com.allset.allset.dto

import com.allset.allset.model.AppliedPromoCode
import com.allset.allset.model.User
import com.allset.allset.model.UserRole
import java.time.Instant

data class AdminUserResponse(
    val id: String?,
    val email: String,
    val name: String,
    val picture: String?,
    val phoneNumber: String?,
    val dateOfBirth: String?,
    val status: String?,
    val isPaid: Boolean,
    val role: UserRole,
    val lastSeenAt: Instant?,
    val invitationCount: Int,
    val appliedPromoCodes: List<AppliedPromoCode>
)

fun User.toAdminResponse(invitationCount: Int) = AdminUserResponse(
    id = this.id,
    email = this.email,
    name = this.name,
    picture = this.picture,
    phoneNumber = this.phoneNumber,
    dateOfBirth = this.dateOfBirth,
    status = this.status,
    isPaid = this.isPaid,
    role = this.role,
    lastSeenAt = this.lastSeenAt,
    invitationCount = invitationCount,
    appliedPromoCodes = this.appliedPromoCodes
)
