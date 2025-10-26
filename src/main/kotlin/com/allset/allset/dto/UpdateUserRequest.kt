package com.allset.allset.dto

data class UpdateUserRequest(
    val name: String? = null,
    val picture: String? = null,
    val phoneNumber: String? = null,
    val dateOfBirth: String? = null
)
