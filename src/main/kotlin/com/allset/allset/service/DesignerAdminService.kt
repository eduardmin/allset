package com.allset.allset.service

import com.allset.allset.dto.ProcessPayoutRequest
import com.allset.allset.dto.SubmissionReviewRequest
import com.allset.allset.model.DesignerApplicationStatus
import com.allset.allset.model.DesignerProfile
import com.allset.allset.model.EarningStatus
import com.allset.allset.model.PayoutRequest
import com.allset.allset.model.PayoutStatus
import com.allset.allset.model.ReviewNote
import com.allset.allset.model.SubmissionStatus
import com.allset.allset.model.TemplateSubmission
import com.allset.allset.model.User
import com.allset.allset.model.UserRole
import com.allset.allset.repository.DesignerEarningRepository
import com.allset.allset.repository.PayoutRequestRepository
import com.allset.allset.repository.TemplateSubmissionRepository
import com.allset.allset.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class DesignerAdminService(
    private val userRepository: UserRepository,
    private val submissionRepository: TemplateSubmissionRepository,
    private val payoutRepository: PayoutRequestRepository,
    private val earningRepository: DesignerEarningRepository,
    private val authenticationService: AuthenticationService
) {
    private val logger = LoggerFactory.getLogger(DesignerAdminService::class.java)

    private fun currentAdmin(): User {
        val id = authenticationService.getCurrentUserId()
        return userRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not found")
        }
    }

    // ---- Designer applications / accounts ----

    fun listDesigners(): List<User> =
        userRepository.findAll().filter { it.role == UserRole.DESIGNER }

    fun listApplications(): List<User> =
        userRepository.findAll().filter { it.designerApplicationStatus == DesignerApplicationStatus.PENDING }

    fun approveApplication(userId: String): User {
        val user = userRepository.findById(userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        }
        val profile = (user.designerProfile ?: DesignerProfile()).copy(approvedAt = Instant.now())
        val updated = user.copy(
            role = UserRole.DESIGNER,
            designerApplicationStatus = DesignerApplicationStatus.APPROVED,
            designerProfile = profile
        )
        logger.info("Designer application approved: userId=$userId")
        return userRepository.save(updated)
    }

    fun rejectApplication(userId: String): User {
        val user = userRepository.findById(userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        }
        val updated = user.copy(designerApplicationStatus = DesignerApplicationStatus.REJECTED)
        return userRepository.save(updated)
    }

    fun setCommissionRate(userId: String, rate: Double): User {
        if (rate < 0.0 || rate > 1.0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "commissionRate must be between 0 and 1")
        }
        val user = userRepository.findById(userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        }
        val profile = (user.designerProfile ?: DesignerProfile()).copy(commissionRate = rate)
        logger.info("Designer commission rate updated: userId=$userId, rate=$rate")
        return userRepository.save(user.copy(designerProfile = profile))
    }

    // ---- Submission review ----

    fun listSubmissions(status: String?): List<TemplateSubmission> {
        if (status.isNullOrBlank()) {
            return submissionRepository.findAllByOrderByLastModifiedAtDesc()
        }
        val parsed = runCatching { SubmissionStatus.valueOf(status.uppercase()) }.getOrNull()
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: $status")
        return submissionRepository.findAllByStatusOrderBySubmittedAtDesc(parsed)
    }

    fun getSubmission(id: String): TemplateSubmission =
        submissionRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found")
        }

    fun reviewSubmission(id: String, request: SubmissionReviewRequest): TemplateSubmission {
        val admin = currentAdmin()
        val submission = getSubmission(id)
        val now = Instant.now()

        val targetStatus = when (request.action.uppercase()) {
            "APPROVE" -> {
                requireStatus(submission, SubmissionStatus.SUBMITTED, SubmissionStatus.CHANGES_REQUESTED)
                SubmissionStatus.APPROVED
            }
            "REQUEST_CHANGES" -> {
                if (request.message.isNullOrBlank()) {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "A message is required when requesting changes")
                }
                requireStatus(submission, SubmissionStatus.SUBMITTED)
                SubmissionStatus.CHANGES_REQUESTED
            }
            "REJECT" -> {
                requireStatus(submission, SubmissionStatus.SUBMITTED, SubmissionStatus.CHANGES_REQUESTED)
                SubmissionStatus.REJECTED
            }
            "START_DEVELOPMENT" -> {
                requireStatus(submission, SubmissionStatus.APPROVED)
                SubmissionStatus.IN_DEVELOPMENT
            }
            "RELEASE" -> {
                requireStatus(submission, SubmissionStatus.IN_DEVELOPMENT, SubmissionStatus.APPROVED)
                if (request.linkedTemplateId.isNullOrBlank()) {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "linkedTemplateId is required to release")
                }
                SubmissionStatus.RELEASED
            }
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown action: ${request.action}")
        }

        val note = request.message?.takeIf { it.isNotBlank() }?.let {
            ReviewNote(
                authorId = admin.id ?: "admin",
                authorName = admin.name,
                message = it,
                fromStatus = submission.status,
                toStatus = targetStatus,
                createdAt = now
            )
        }

        val updated = submission.copy(
            status = targetStatus,
            reviewNotes = if (note != null) submission.reviewNotes + note else submission.reviewNotes,
            commissionRate = request.commissionRate ?: submission.commissionRate,
            linkedTemplateId = request.linkedTemplateId ?: submission.linkedTemplateId,
            reviewedAt = now,
            releasedAt = if (targetStatus == SubmissionStatus.RELEASED) now else submission.releasedAt,
            lastModifiedAt = now
        )
        logger.info("Submission $id reviewed: ${submission.status} -> $targetStatus by adminId=${admin.id}")
        return submissionRepository.save(updated)
    }

    private fun requireStatus(submission: TemplateSubmission, vararg allowed: SubmissionStatus) {
        if (submission.status !in allowed) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Action not allowed from status ${submission.status}"
            )
        }
    }

    // ---- Payouts ----

    fun listPayouts(status: String?): List<PayoutRequest> {
        if (status.isNullOrBlank()) {
            return payoutRepository.findAllByOrderByRequestedAtDesc()
        }
        val parsed = runCatching { PayoutStatus.valueOf(status.uppercase()) }.getOrNull()
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: $status")
        return payoutRepository.findAllByStatusOrderByRequestedAtDesc(parsed)
    }

    fun processPayout(id: String, request: ProcessPayoutRequest): PayoutRequest {
        val payout = payoutRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Payout not found")
        }
        val now = Instant.now()

        val updated = when (request.action.uppercase()) {
            "APPROVE" -> {
                requirePayoutStatus(payout, PayoutStatus.REQUESTED)
                payout.copy(status = PayoutStatus.APPROVED, adminNote = request.adminNote)
            }
            "MARK_PAID" -> {
                requirePayoutStatus(payout, PayoutStatus.REQUESTED, PayoutStatus.APPROVED)
                // Settle the reserved earnings.
                payout.earningIds.forEach { earningId ->
                    earningRepository.findById(earningId).ifPresent { earning ->
                        earningRepository.save(earning.copy(status = EarningStatus.PAID, paidAt = now))
                    }
                }
                payout.copy(status = PayoutStatus.PAID, adminNote = request.adminNote, processedAt = now)
            }
            "REJECT" -> {
                requirePayoutStatus(payout, PayoutStatus.REQUESTED, PayoutStatus.APPROVED)
                // Release the reserved earnings back to available.
                payout.earningIds.forEach { earningId ->
                    earningRepository.findById(earningId).ifPresent { earning ->
                        earningRepository.save(earning.copy(payoutRequestId = null))
                    }
                }
                payout.copy(status = PayoutStatus.REJECTED, adminNote = request.adminNote, processedAt = now)
            }
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown action: ${request.action}")
        }
        logger.info("Payout $id processed: ${payout.status} -> ${updated.status}")
        return payoutRepository.save(updated)
    }

    private fun requirePayoutStatus(payout: PayoutRequest, vararg allowed: PayoutStatus) {
        if (payout.status !in allowed) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Action not allowed from status ${payout.status}")
        }
    }
}
