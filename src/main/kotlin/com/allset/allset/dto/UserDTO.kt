package com.allset.allset.dto

import com.allset.allset.model.AppliedPromoCode
import com.allset.allset.model.User
import com.allset.allset.model.UserRole
import java.time.Instant

data class UserDTO(
    val id: String?,
    val email: String,
    val name: String,
    val picture: String?,
    val phoneNumber: String?,
    val dateOfBirth: String?,
    val status: String?,
    val role: UserRole,
    val lastSeenAt: Instant?,
    val appliedPromoCodes: List<AppliedPromoCode>
)

fun User.toDTO(): UserDTO = UserDTO(
    id = this.id,
    email = this.email,
    name = this.name,
    picture = this.picture,
    phoneNumber = this.phoneNumber,
    dateOfBirth = this.dateOfBirth,
    status = this.status,
    role = this.role,
    lastSeenAt = this.lastSeenAt,
    appliedPromoCodes = this.appliedPromoCodes
)

fun UserDTO.toEntity(existing: User): User = existing.copy(
    name = this.name,
    picture = this.picture,
    phoneNumber = this.phoneNumber,
    dateOfBirth = this.dateOfBirth,
    status = this.status,
    appliedPromoCodes = this.appliedPromoCodes
)
