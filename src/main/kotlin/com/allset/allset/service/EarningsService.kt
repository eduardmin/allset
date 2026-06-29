package com.allset.allset.service

import com.allset.allset.dto.EarningsSummaryResponse
import com.allset.allset.dto.SalesByTemplateResponse
import com.allset.allset.model.DesignerEarning
import com.allset.allset.model.EarningStatus
import com.allset.allset.model.Payment
import com.allset.allset.repository.DesignerEarningRepository
import com.allset.allset.repository.InvitationRepository
import com.allset.allset.repository.TemplateSubmissionRepository
import com.allset.allset.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

@Service
class EarningsService(
    private val earningRepository: DesignerEarningRepository,
    private val submissionRepository: TemplateSubmissionRepository,
    private val invitationRepository: InvitationRepository,
    private val userRepository: UserRepository,
    private val authenticationService: AuthenticationService
) {
    private val logger = LoggerFactory.getLogger(EarningsService::class.java)

    private val defaultCommissionRate = 0.05

    /**
     * Called from the payment-success path (Idram/ArCa). Best-effort: never throws
     * back into the payment flow. Attributes a commission to the designer whose
     * released submission is linked to the purchased invitation's template.
     */
    fun recordEarningForPayment(payment: Payment) {
        try {
            val paymentId = payment.id ?: return
            if (earningRepository.existsByPaymentId(paymentId)) {
                logger.info("Earning already recorded for paymentId=$paymentId")
                return
            }
            val invitationId = payment.invitationId ?: return
            val invitation = invitationRepository.findById(invitationId).orElse(null) ?: return

            val submission = submissionRepository.findByLinkedTemplateId(invitation.templateId) ?: run {
                logger.info("No designer submission linked to templateId=${invitation.templateId}; skipping earning")
                return
            }

            val designer = userRepository.findById(submission.designerId).orElse(null)
            val rate = submission.commissionRate
                ?: designer?.designerProfile?.commissionRate
                ?: defaultCommissionRate

            val saleAmount = payment.amount
            val earnedAmount = saleAmount
                .multiply(BigDecimal.valueOf(rate))
                .setScale(2, RoundingMode.HALF_UP)

            val earning = DesignerEarning(
                designerId = submission.designerId,
                submissionId = submission.id,
                linkedTemplateId = submission.linkedTemplateId,
                paymentId = paymentId,
                invitationId = invitationId,
                saleAmount = saleAmount,
                commissionRate = rate,
                earnedAmount = earnedAmount,
                // v1: no hold window, earnings are immediately available for cash-out.
                status = EarningStatus.AVAILABLE,
                availableAt = Instant.now()
            )
            earningRepository.save(earning)
            logger.info("Recorded designer earning: designerId=${submission.designerId}, amount=$earnedAmount, paymentId=$paymentId")
        } catch (e: Exception) {
            logger.error("Failed to record designer earning for paymentId=${payment.id}", e)
        }
    }

    fun listEarnings(): List<DesignerEarning> {
        val designerId = authenticationService.getCurrentUserId()
        return earningRepository.findAllByDesignerIdOrderByCreatedAtDesc(designerId)
    }

    fun getSummary(): EarningsSummaryResponse {
        val designerId = authenticationService.getCurrentUserId()
        val earnings = earningRepository.findAllByDesignerIdOrderByCreatedAtDesc(designerId)

        fun sumWhere(predicate: (DesignerEarning) -> Boolean): BigDecimal =
            earnings.filter(predicate).fold(BigDecimal.ZERO) { acc, e -> acc.add(e.earnedAmount) }

        return EarningsSummaryResponse(
            lifetime = sumWhere { true },
            // PENDING (hold window) + AVAILABLE earnings already reserved by a payout request.
            pending = sumWhere {
                it.status == EarningStatus.PENDING ||
                    (it.status == EarningStatus.AVAILABLE && it.payoutRequestId != null)
            },
            // Only unreserved AVAILABLE earnings can be cashed out.
            available = sumWhere { it.status == EarningStatus.AVAILABLE && it.payoutRequestId == null },
            paid = sumWhere { it.status == EarningStatus.PAID },
            salesCount = earnings.size.toLong()
        )
    }

    fun salesByTemplate(): List<SalesByTemplateResponse> {
        val designerId = authenticationService.getCurrentUserId()
        val earnings = earningRepository.findAllByDesignerIdOrderByCreatedAtDesc(designerId)

        return earnings.groupBy { it.submissionId }.map { (submissionId, group) ->
            val submission = submissionId?.let { submissionRepository.findById(it).orElse(null) }
            SalesByTemplateResponse(
                linkedTemplateId = group.first().linkedTemplateId,
                submissionId = submissionId,
                title = submission?.title ?: emptyMap(),
                salesCount = group.size.toLong(),
                totalEarned = group.fold(BigDecimal.ZERO) { acc, e -> acc.add(e.earnedAmount) }
            )
        }.sortedByDescending { it.salesCount }
    }
}
