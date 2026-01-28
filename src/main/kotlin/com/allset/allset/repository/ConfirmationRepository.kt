package com.allset.allset.repository

import com.allset.allset.model.Confirmation
import com.allset.allset.model.ConfirmationCreator
import com.allset.allset.model.ConfirmationStatus
import com.allset.allset.model.GuestSide
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ConfirmationRepository : MongoRepository<Confirmation, String> {
    fun findAllByInvitationIdAndDeletedFalse(invitationId: String): List<Confirmation>
    
    // Added by me filter
    fun findAllByInvitationIdAndDeletedFalseAndCreatedBy(
        invitationId: String, 
        createdBy: ConfirmationCreator
    ): List<Confirmation>
    
    // Show deleted/hidden
    fun findAllByInvitationIdAndDeletedTrue(invitationId: String): List<Confirmation>
    
    // Show only confirmed
    fun findAllByInvitationIdAndDeletedFalseAndStatus(
        invitationId: String, 
        status: ConfirmationStatus
    ): List<Confirmation>
    
    // Show by guest side
    fun findAllByInvitationIdAndDeletedFalseAndGuestSide(
        invitationId: String, 
        guestSide: GuestSide
    ): List<Confirmation>
    
    // Show without table number
    fun findAllByInvitationIdAndDeletedFalseAndTableNumberIsNull(invitationId: String): List<Confirmation>
}
