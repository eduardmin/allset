package com.allset.allset.dto

data class AdminDashboardStats(
    val totalUsers: Long,
    val totalInvitations: Long,
    val activeInvitations: Long,
    val draftInvitations: Long,
    val expiredInvitations: Long,
    val totalConfirmations: Long,
    val totalPromoCodes: Long,
    val activePromoCodes: Long
)
