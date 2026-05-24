package com.allset.allset.repository

import com.allset.allset.model.Referral
import com.allset.allset.model.ReferralStatus
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ReferralRepository : MongoRepository<Referral, String> {
    fun findAllByReferrerUserId(referrerUserId: String): List<Referral>
    fun findAllByReferredUserId(referredUserId: String): List<Referral>
    fun findByReferredUserIdAndStatus(referredUserId: String, status: ReferralStatus): Referral?
    fun findAllByReferralCode(referralCode: String): List<Referral>
    fun countByReferralCode(referralCode: String): Long
}
