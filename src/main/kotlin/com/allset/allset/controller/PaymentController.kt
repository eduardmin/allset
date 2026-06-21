package com.allset.allset.controller

import com.allset.allset.config.ArcaProperties
import com.allset.allset.model.Payment
import com.allset.allset.service.ArcaService
import com.allset.allset.service.IdramService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

data class InitiatePaymentRequest(
    val invitationId: String,
    val language: String = "EN"
)

@RestController
@RequestMapping("/payments")
class PaymentController(
    private val idramService: IdramService,
    private val arcaService: ArcaService,
    private val arcaProperties: ArcaProperties
) {
    private val logger = LoggerFactory.getLogger(PaymentController::class.java)

    @PostMapping("/idram/initiate")
    fun initiatePayment(@RequestBody request: InitiatePaymentRequest): IdramService.PaymentFormData {
        return idramService.initiatePayment(request.invitationId, request.language)
    }

    @PostMapping("/idram/result")
    fun idramResult(
        @RequestParam("EDP_BILL_NO") billNo: String,
        @RequestParam("EDP_REC_ACCOUNT") recAccount: String,
        @RequestParam("EDP_PAYER_ACCOUNT", required = false) payerAccount: String?,
        @RequestParam("EDP_AMOUNT") amount: String,
        @RequestParam("EDP_TRANS_ID", required = false) transId: String?,
        @RequestParam("EDP_TRANS_DATE", required = false) transDate: String?,
        @RequestParam("EDP_CHECKSUM", required = false) checksum: String?
    ): ResponseEntity<String> {
        logger.info("Idram callback: billNo=$billNo, transId=$transId, checksum present=${checksum != null}")

        if (checksum == null || transId == null || transDate == null || payerAccount == null) {
            val preCheckOk = idramService.handlePreCheck(billNo, recAccount, amount)
            return if (preCheckOk) {
                ResponseEntity.ok("OK")
            } else {
                ResponseEntity.badRequest().body("FAILED")
            }
        }

        val success = idramService.handlePaymentConfirmation(
            billNo, recAccount, payerAccount, amount, transId, transDate, checksum
        )

        return if (success) {
            ResponseEntity.ok("OK")
        } else {
            ResponseEntity.badRequest().body("FAILED")
        }
    }

    @PostMapping("/arca/initiate")
    fun initiateArcaPayment(@RequestBody request: InitiatePaymentRequest): ArcaService.PaymentInitResponse {
        return arcaService.initiatePayment(request.invitationId, request.language)
    }

    @GetMapping("/arca/result")
    fun arcaResult(@RequestParam("orderId") orderId: String): ResponseEntity<Void> {
        logger.info("ArCa return: orderId=$orderId")
        val success = arcaService.finalizeByOrderId(orderId)
        val redirectUrl = if (success) arcaProperties.successUrl else arcaProperties.failUrl
        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, URI.create(redirectUrl).toString())
            .build()
    }

    @GetMapping("/last-summary")
    fun getLastPaymentSummary(): IdramService.PaymentSummary {
        return idramService.getLastPaymentSummary()
    }

    @GetMapping("/my")
    fun getMyPayments(): List<Payment> {
        return idramService.getPaymentsByUser()
    }

    @GetMapping("/{billNo}")
    fun getPayment(@PathVariable billNo: String): Payment {
        return idramService.getPaymentByBillNo(billNo)
    }
}
