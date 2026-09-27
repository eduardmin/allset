package com.allset.allset.dto

import com.allset.allset.model.AppliedPromoCode
import com.allset.allset.model.User
import com.allset.allset.model.UserRole
import org.bson.types.ObjectId
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
    val registeredAt: Instant?,
    val lastSeenAt: Instant?,
    val invitationCount: Int,
    val appliedPromoCodes: List<AppliedPromoCode>,
    val referralCode: String,
    val referredBy: String?,
    val marketingOptIn: Boolean
)

/**
 * Registration time derived from the Mongo ObjectId embedded in the user's id
 * (the first 4 bytes encode the creation timestamp). The User document has no
 * explicit createdAt field, so this works retroactively for every existing user
 * without a migration. Returns null if the id is missing or not a valid ObjectId.
 */
fun User.registeredAt(): Instant? =
    this.id?.takeIf { ObjectId.isValid(it) }?.let { ObjectId(it).date.toInstant() }

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
    registeredAt = this.registeredAt(),
    lastSeenAt = this.lastSeenAt,
    invitationCount = invitationCount,
    appliedPromoCodes = this.appliedPromoCodes,
    referralCode = this.referralCode,
    referredBy = this.referredBy,
    marketingOptIn = this.marketingOptIn
)
