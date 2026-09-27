package com.allset.allset.dto

/**
 * Admin activity buckets. Each list holds the full user records (name, email and
 * all admin-visible info via [AdminUserResponse]) rather than a bare count, so the
 * admin can see who registered / signed in recently.
 */
data class AdminUserActivityStats(
    val registeredLast7Days: List<AdminUserResponse>,
    val registeredLast30Days: List<AdminUserResponse>,
    val loggedInLast7Days: List<AdminUserResponse>
)
