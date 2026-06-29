package com.allset.allset.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

@Document(collection = "users")
data class User(
    @Id
    val id: String? = null,
    val sub: String? = null,
    val email: String,
    val name: String,
    val picture: String?,
    val phoneNumber: String? = null,
    val dateOfBirth: String? = null,
    val isPaid: Boolean = false,
    val appliedPromoCodes: List<AppliedPromoCode> = emptyList(),
    val status: String? = null,
    @Field("role")
    val role: UserRole = UserRole.USER,
    @Indexed(unique = true)
    val referralCode: String = generateReferralCode(),
    val referredBy: String? = null,
    val marketingOptIn: Boolean = false,
    val lastSeenAt: Instant? = null,
    val designerApplicationStatus: DesignerApplicationStatus = DesignerApplicationStatus.NONE,
    val designerProfile: DesignerProfile? = null
)

enum class UserRole {
    USER,
    ADMIN,
    DESIGNER
}

enum class DesignerApplicationStatus {
    NONE,
    PENDING,
    APPROVED,
    REJECTED
}

data class DesignerProfile(
    val fullName: String? = null,
    val portfolioUrl: String? = null,
    val bio: String? = null,
    // Where/how the designer wants to be paid out (free-form for the manual v1 flow).
    val payoutMethod: String? = null,
    val payoutDetails: String? = null,
    // Default share of each sale that accrues to this designer (0.0 - 1.0).
    val commissionRate: Double = 0.05,
    val appliedAt: Instant? = null,
    val approvedAt: Instant? = null
)

private val REFERRAL_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

fun generateReferralCode(length: Int = 7): String =
    (1..length).map { REFERRAL_CHARS.random() }.joinToString("")
