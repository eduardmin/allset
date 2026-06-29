package com.allset.allset.service

import com.allset.allset.dto.CreatePayoutRequest
import com.allset.allset.model.EarningStatus
import com.allset.allset.model.PayoutRequest
import com.allset.allset.model.PayoutStatus
import com.allset.allset.repository.DesignerEarningRepository
import com.allset.allset.repository.PayoutRequestRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.Instant

@Service
class PayoutService(
    private val payoutRepository: PayoutRequestRepository,
    private val earningRepository: DesignerEarningRepository,
    private val authenticationService: AuthenticationService
) {
    private val logger = LoggerFactory.getLogger(PayoutService::class.java)

    fun listMyPayouts(): List<PayoutRequest> {
        val designerId = authenticationService.getCurrentUserId()
        return payoutRepository.findAllByDesignerIdOrderByRequestedAtDesc(designerId)
    }

    /**
     * Cashes out the designer's entire currently-available (unreserved) balance.
     * The client-supplied amount is informational; the server reserves the real
     * available earnings to avoid rounding/double-spend issues in the manual v1 flow.
     */
    fun requestPayout(request: CreatePayoutRequest): PayoutRequest {
        val designerId = authenticationService.getCurrentUserId()

        val existing = payoutRepository.findAllByDesignerIdOrderByRequestedAtDesc(designerId)
        if (existing.any { it.status == PayoutStatus.REQUESTED || it.status == PayoutStatus.APPROVED }) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "You already have a payout in progress")
        }

        val available = earningRepository
            .findAllByDesignerIdAndStatus(designerId, EarningStatus.AVAILABLE)
            .filter { it.payoutRequestId == null }

        if (available.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No available balance to cash out")
        }

        val amount = available.fold(BigDecimal.ZERO) { acc, e -> acc.add(e.earnedAmount) }

        val payout = payoutRepository.save(
            PayoutRequest(
                designerId = designerId,
                amount = amount,
                status = PayoutStatus.REQUESTED,
                method = request.method,
                details = request.details,
                earningIds = available.mapNotNull { it.id }
            )
        )

        // Reserve the earnings against this payout (kept AVAILABLE until paid).
        available.forEach { earning ->
            earningRepository.save(earning.copy(payoutRequestId = payout.id))
        }

        logger.info("Payout requested: designerId=$designerId, amount=$amount, payoutId=${payout.id}")
        return payout
    }
}
