package com.allset.allset.service

import com.allset.allset.config.IdramProperties
import com.allset.allset.model.Payment
import com.allset.allset.model.PaymentStatus
import com.allset.allset.repository.PaymentRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

@Service
class IdramService(
    private val paymentRepository: PaymentRepository,
    private val idramProperties: IdramProperties,
    private val authenticationService: AuthenticationService,
    private val invitationService: InvitationService
) {
    private val logger = LoggerFactory.getLogger(IdramService::class.java)

    data class PaymentFormData(
        val actionUrl: String = "https://banking.idram.am/Payment/GetPayment",
        val edpLanguage: String,
        val edpRecAccount: String,
        val edpDescription: String,
        val edpAmount: String,
        val edpBillNo: String
    )

    fun initiatePayment(invitationId: String, amount: BigDecimal, language: String): PaymentFormData {
        val userId = authenticationService.getCurrentUserId()

        invitationService.validateForPayment(invitationId)

        val billNo = "ALLSET-${UUID.randomUUID().toString().take(12).uppercase()}"

        val payment = Payment(
            userId = userId,
            invitationId = invitationId,
            amount = amount,
            billNo = billNo
        )
        paymentRepository.save(payment)

        logger.info("Payment initiated: billNo=$billNo, amount=$amount, userId=$userId")

        return PaymentFormData(
            edpLanguage = language.uppercase(),
            edpRecAccount = idramProperties.recAccount,
            edpDescription = "AllSet Wedding Invitation",
            edpAmount = amount.toPlainString(),
            edpBillNo = billNo
        )
    }

    fun handlePreCheck(billNo: String, recAccount: String, amount: String): Boolean {
        logger.info("Idram pre-check: billNo=$billNo, recAccount=$recAccount, amount=$amount")

        val payment = paymentRepository.findByBillNo(billNo)
        if (payment == null) {
            logger.warn("Pre-check failed: payment not found for billNo=$billNo")
            return false
        }

        if (payment.status != PaymentStatus.PENDING) {
            logger.warn("Pre-check failed: payment status is ${payment.status} for billNo=$billNo")
            return false
        }

        if (recAccount != idramProperties.recAccount) {
            logger.warn("Pre-check failed: recAccount mismatch for billNo=$billNo")
            return false
        }

        val expectedAmount = payment.amount.toPlainString()
        if (amount != expectedAmount) {
            logger.warn("Pre-check failed: amount mismatch for billNo=$billNo (expected=$expectedAmount, received=$amount)")
            return false
        }

        logger.info("Pre-check passed for billNo=$billNo")
        return true
    }

    fun handlePaymentConfirmation(
        billNo: String,
        recAccount: String,
        payerAccount: String,
        amount: String,
        transId: String,
        transDate: String,
        checksum: String
    ): Boolean {
        logger.info("Idram payment confirmation: billNo=$billNo, transId=$transId")

        val expectedChecksum = computeChecksum(recAccount, amount, billNo, payerAccount, transId, transDate)
        if (!expectedChecksum.equals(checksum, ignoreCase = true)) {
            logger.warn("Checksum mismatch for billNo=$billNo")
            return false
        }

        val payment = paymentRepository.findByBillNo(billNo)
        if (payment == null) {
            logger.warn("Payment not found for billNo=$billNo")
            return false
        }

        if (payment.status == PaymentStatus.COMPLETED) {
            logger.info("Payment already completed for billNo=$billNo")
            return true
        }

        val updated = payment.copy(
            status = PaymentStatus.COMPLETED,
            transactionId = transId,
            payerAccount = payerAccount,
            transactionDate = transDate,
            completedAt = Instant.now()
        )
        paymentRepository.save(updated)

        logger.info("Payment completed: billNo=$billNo, transId=$transId")

        if (updated.invitationId != null) {
            try {
                invitationService.publishDraft(updated.invitationId)
                logger.info("Invitation auto-published after payment: invitationId=${updated.invitationId}")
            } catch (e: Exception) {
                logger.error("Failed to auto-publish invitation after payment: invitationId=${updated.invitationId}", e)
            }
        }

        return true
    }

    fun getPaymentsByUser(): List<Payment> {
        val userId = authenticationService.getCurrentUserId()
        return paymentRepository.findAllByUserId(userId)
    }

    fun getPaymentByBillNo(billNo: String): Payment {
        return paymentRepository.findByBillNo(billNo)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found")
    }

    private fun computeChecksum(
        recAccount: String, amount: String, billNo: String,
        payerAccount: String, transId: String, transDate: String
    ): String {
        val raw = "$recAccount:$amount:${idramProperties.secretKey}:$billNo:$payerAccount:$transId:$transDate"
        val md5 = MessageDigest.getInstance("MD5")
        return md5.digest(raw.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
