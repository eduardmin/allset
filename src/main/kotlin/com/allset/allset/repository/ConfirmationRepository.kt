package com.allset.allset.repository

import com.allset.allset.model.Confirmation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ConfirmationRepository : MongoRepository<Confirmation, String> {
    fun findAllByInvitationId(invitationId: String): List<Confirmation>
}
