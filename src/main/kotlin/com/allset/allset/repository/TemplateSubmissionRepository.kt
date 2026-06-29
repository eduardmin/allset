package com.allset.allset.repository

import com.allset.allset.model.SubmissionStatus
import com.allset.allset.model.TemplateSubmission
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TemplateSubmissionRepository : MongoRepository<TemplateSubmission, String> {
    fun findAllByDesignerIdOrderByLastModifiedAtDesc(designerId: String): List<TemplateSubmission>
    fun findAllByStatusOrderBySubmittedAtDesc(status: SubmissionStatus): List<TemplateSubmission>
    fun findAllByOrderByLastModifiedAtDesc(): List<TemplateSubmission>
    fun findByLinkedTemplateId(linkedTemplateId: String): TemplateSubmission?
}
