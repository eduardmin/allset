package com.allset.allset.dto

import com.allset.allset.model.AppliedPromoCode
import com.allset.allset.model.User

data class UserDTO(
    val id: String?,
    val email: String,
    val name: String,
    val picture: String?,
    val appliedPromoCodes: List<AppliedPromoCode>
)

fun User.toDTO(): UserDTO = UserDTO(
    id = this.id,
    email = this.email,
    name = this.name,
    picture = this.picture,
    appliedPromoCodes = this.appliedPromoCodes
)

fun UserDTO.toEntity(existing: User): User = existing.copy(
    name = this.name,
    picture = this.picture
)
