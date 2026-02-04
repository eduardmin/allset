package com.allset.allset.repository

import com.allset.allset.model.Invitation
import com.allset.allset.model.InvitationStatus
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface InvitationRepository : MongoRepository<Invitation, String> {
    fun findAllByOwnerId(ownerId: String): List<Invitation>
    fun findAllByOwnerIdAndStatus(ownerId: String, status: InvitationStatus): List<Invitation>
    fun findByUrlExtension(urlExtension: String): Invitation?
}
