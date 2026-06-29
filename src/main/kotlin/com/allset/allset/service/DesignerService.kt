package com.allset.allset.service

import com.allset.allset.dto.CreateSubmissionRequest
import com.allset.allset.dto.DesignerApplyRequest
import com.allset.allset.dto.DesignerMeResponse
import com.allset.allset.dto.DesignerStatusResponse
import com.allset.allset.dto.UpdateDesignerProfileRequest
import com.allset.allset.dto.UpdateSubmissionRequest
import com.allset.allset.model.DesignerApplicationStatus
import com.allset.allset.model.DesignerProfile
import com.allset.allset.model.SubmissionStatus
import com.allset.allset.model.TemplateSubmission
import com.allset.allset.model.User
import com.allset.allset.model.UserRole
import com.allset.allset.repository.TemplateSubmissionRepository
import com.allset.allset.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class DesignerService(
    private val userRepository: UserRepository,
    private val submissionRepository: TemplateSubmissionRepository,
    private val authenticationService: AuthenticationService
) {
    private val logger = LoggerFactory.getLogger(DesignerService::class.java)

    private fun currentUser(): User {
        val userId = authenticationService.getCurrentUserId()
        return userRepository.findById(userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        }
    }

    private fun requireDesigner(): User {
        val user = currentUser()
        if (user.role != UserRole.DESIGNER && user.role != UserRole.ADMIN) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Designer access required")
        }
        return user
    }

    // ---- Status / profile ----

    fun getStatus(): DesignerStatusResponse {
        val user = currentUser()
        return DesignerStatusResponse(
            role = user.role,
            applicationStatus = user.designerApplicationStatus,
            isDesigner = user.role == UserRole.DESIGNER || user.role == UserRole.ADMIN
        )
    }

    fun getMe(): DesignerMeResponse {
        val user = requireDesigner()
        val p = user.designerProfile
        return DesignerMeResponse(
            id = user.id,
            email = user.email,
            name = user.name,
            picture = user.picture,
            role = user.role,
            applicationStatus = user.designerApplicationStatus,
            fullName = p?.fullName,
            portfolioUrl = p?.portfolioUrl,
            bio = p?.bio,
            payoutMethod = p?.payoutMethod,
            payoutDetails = p?.payoutDetails,
            commissionRate = p?.commissionRate ?: 0.05
        )
    }

    fun apply(request: DesignerApplyRequest): DesignerStatusResponse {
        val user = currentUser()

        if (user.role == UserRole.DESIGNER || user.role == UserRole.ADMIN) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "You are already a designer")
        }
        if (user.designerApplicationStatus == DesignerApplicationStatus.PENDING) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Your application is already under review")
        }
        if (request.fullName.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name is required")
        }

        val profile = (user.designerProfile ?: DesignerProfile()).copy(
            fullName = request.fullName,
            portfolioUrl = request.portfolioUrl,
            bio = request.bio,
            appliedAt = Instant.now()
        )

        userRepository.save(
            user.copy(
                designerApplicationStatus = DesignerApplicationStatus.PENDING,
                designerProfile = profile
            )
        )
        logger.info("Designer application submitted by userId=${user.id}")
        return getStatus()
    }

    fun updateMe(request: UpdateDesignerProfileRequest): DesignerMeResponse {
        val user = requireDesigner()
        val current = user.designerProfile ?: DesignerProfile()
        val updated = current.copy(
            fullName = request.fullName ?: current.fullName,
            portfolioUrl = request.portfolioUrl ?: current.portfolioUrl,
            bio = request.bio ?: current.bio,
            payoutMethod = request.payoutMethod ?: current.payoutMethod,
            payoutDetails = request.payoutDetails ?: current.payoutDetails
        )
        userRepository.save(user.copy(designerProfile = updated))
        return getMe()
    }

    // ---- Submissions ----

    fun listSubmissions(): List<TemplateSubmission> {
        val user = requireDesigner()
        return submissionRepository.findAllByDesignerIdOrderByLastModifiedAtDesc(user.id!!)
    }

    fun getSubmission(id: String): TemplateSubmission {
        val user = requireDesigner()
        val submission = submissionRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found")
        }
        if (submission.designerId != user.id && user.role != UserRole.ADMIN) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Not your submission")
        }
        return submission
    }

    fun createSubmission(request: CreateSubmissionRequest): TemplateSubmission {
        val user = requireDesigner()
        val submission = TemplateSubmission(
            designerId = user.id!!,
            title = request.title ?: emptyMap(),
            description = request.description ?: emptyMap(),
            type = request.type ?: "WEDDING",
            figmaUrl = request.figmaUrl,
            coverImage = request.coverImage,
            previewImages = request.previewImages ?: emptyList(),
            spec = request.spec ?: com.allset.allset.model.SubmissionSpec(),
            status = SubmissionStatus.DRAFT
        )
        return submissionRepository.save(submission)
    }

    fun updateSubmission(id: String, request: UpdateSubmissionRequest): TemplateSubmission {
        val submission = getSubmission(id)
        if (submission.status != SubmissionStatus.DRAFT &&
            submission.status != SubmissionStatus.CHANGES_REQUESTED
        ) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Submission can only be edited while in DRAFT or CHANGES_REQUESTED"
            )
        }
        val updated = submission.copy(
            title = request.title ?: submission.title,
            description = request.description ?: submission.description,
            type = request.type ?: submission.type,
            figmaUrl = request.figmaUrl ?: submission.figmaUrl,
            coverImage = request.coverImage ?: submission.coverImage,
            previewImages = request.previewImages ?: submission.previewImages,
            spec = request.spec ?: submission.spec,
            lastModifiedAt = Instant.now()
        )
        return submissionRepository.save(updated)
    }

    fun submitForReview(id: String): TemplateSubmission {
        val submission = getSubmission(id)
        if (submission.status != SubmissionStatus.DRAFT &&
            submission.status != SubmissionStatus.CHANGES_REQUESTED
        ) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Submission already in review or finalized")
        }
        if (submission.figmaUrl.isNullOrBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "A Figma link is required before submitting")
        }
        if (submission.title.values.all { it.isBlank() }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "A title is required before submitting")
        }
        val updated = submission.copy(
            status = SubmissionStatus.SUBMITTED,
            submittedAt = Instant.now(),
            lastModifiedAt = Instant.now()
        )
        logger.info("Submission $id submitted for review by designerId=${submission.designerId}")
        return submissionRepository.save(updated)
    }

    fun deleteSubmission(id: String) {
        val submission = getSubmission(id)
        if (submission.status == SubmissionStatus.RELEASED ||
            submission.status == SubmissionStatus.IN_DEVELOPMENT
        ) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Released or in-development submissions cannot be deleted")
        }
        submissionRepository.delete(submission)
    }
}
