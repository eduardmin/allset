package com.allset.allset.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "referrals")
data class Referral(
    @Id val id: String? = null,
    val referrerUserId: String,
    val referredUserId: String,
    val referralCode: String,
    val rewardPromoCodeId: String? = null,
    val status: ReferralStatus = ReferralStatus.PENDING,
    val createdAt: Instant = Instant.now(),
    val rewardedAt: Instant? = null
)

enum class ReferralStatus {
    PENDING,
    REWARDED
}
