package com.allset.allset.service

import com.allset.allset.config.ArcaProperties
import com.allset.allset.model.Payment
import com.allset.allset.model.PaymentProvider
import com.allset.allset.model.PaymentStatus
import com.allset.allset.repository.InvitationRepository
import com.allset.allset.repository.PaymentRepository
import com.allset.allset.repository.UserRepository
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.UUID

@Service
class ArcaService(
    private val paymentRepository: PaymentRepository,
    private val arcaProperties: ArcaProperties,
    private val authenticationService: AuthenticationService,
    private val invitationService: InvitationService,
    private val referralService: ReferralService,
    private val templateService: TemplateService,
    private val pricingService: PricingService,
    private val userRepository: UserRepository,
    private val invitationRepository: InvitationRepository
) {
    private val logger = LoggerFactory.getLogger(ArcaService::class.java)
    private val webClient = WebClient.builder().build()
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    private val baseUrl: String
        get() = arcaProperties.baseUrl.trimEnd('/')

    data class PaymentInitResponse(
        val formUrl: String,
        val orderId: String
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class RegisterResponse(
        val orderId: String? = null,
        val formUrl: String? = null,
        val errorCode: String? = null,
        val errorMessage: String? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class OrderStatusResponse(
        val orderStatus: Int? = null,
        val actionCode: Int? = null,
        val actionCodeDescription: String? = null,
        val errorCode: String? = null,
        val errorMessage: String? = null,
        val amount: Long? = null
    )

    fun initiatePayment(invitationId: String, language: String): PaymentInitResponse {
        val userId = authenticationService.getCurrentUserId()

        val invitation = invitationService.validateForPayment(invitationId)

        val user = userRepository.findById(userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        }
        val templateBasePrice = templateService.getBasePriceForTemplate(invitation.templateId)
        val pricingSummary = pricingService.summarize(user.appliedPromoCodes, templateBasePrice)
        val amount = pricingSummary.finalPrice

        // ArCa expects the amount in minimal units (lumas) = AMD * 100
        val amountMinorUnits = amount.multiply(BigDecimal(100)).toBigInteger().toString()

        val orderNumber = "ALLSET-CARD-${UUID.randomUUID().toString().take(12).uppercase()}"

        val form = LinkedMultiValueMap<String, String>().apply {
            add("userName", arcaProperties.userName)
            add("password", arcaProperties.password)
            add("orderNumber", orderNumber)
            add("amount", amountMinorUnits)
            add("currency", arcaProperties.currency)
            add("returnUrl", arcaProperties.returnUrl)
            add("description", "AllSet Wedding Invitation")
            add("language", language.lowercase())
            add("pageView", "DESKTOP")
        }

        val rawResponse = webClient.post()
            .uri("$baseUrl/register.do")
            .body(BodyInserters.fromFormData(form))
            .retrieve()
            .bodyToMono<String>()
            .block()

        val response = rawResponse?.let { objectMapper.readValue(it, RegisterResponse::class.java) }

        if (response?.orderId == null || response.formUrl == null) {
            logger.warn("ArCa register failed: orderNumber=$orderNumber, errorCode=${response?.errorCode}, errorMessage=${response?.errorMessage}")
            throw ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Failed to register payment: ${response?.errorMessage ?: "unknown error"}"
            )
        }

        val payment = Payment(
            userId = userId,
            invitationId = invitationId,
            amount = amount,
            billNo = orderNumber,
            status = PaymentStatus.FAILED,
            provider = PaymentProvider.ARCA,
            providerOrderId = response.orderId,
            language = language.lowercase()
        )
        paymentRepository.save(payment)

        logger.info("ArCa payment initiated: orderNumber=$orderNumber, orderId=${response.orderId}, amount=$amount, userId=$userId")

        return PaymentInitResponse(formUrl = response.formUrl, orderId = response.orderId)
    }

    /**
     * Finalizes a payment after the customer returns from the ArCa gateway.
     * Returns true if the order was successfully paid (orderStatus == 2).
     */
    fun finalizeByOrderId(orderId: String): Boolean {
        logger.info("ArCa finalize: orderId=$orderId")

        val payment = paymentRepository.findByProviderOrderId(orderId)
        if (payment == null) {
            logger.warn("ArCa finalize: payment not found for orderId=$orderId")
            return false
        }

        if (payment.status == PaymentStatus.SUCCESS) {
            logger.info("ArCa finalize: payment already completed for orderId=$orderId")
            return true
        }

        val form = LinkedMultiValueMap<String, String>().apply {
            add("userName", arcaProperties.userName)
            add("password", arcaProperties.password)
            add("orderId", orderId)
        }

        val rawStatus = webClient.post()
            .uri("$baseUrl/getOrderStatusExtended.do")
            .body(BodyInserters.fromFormData(form))
            .retrieve()
            .bodyToMono<String>()
            .block()

        val status = rawStatus?.let { objectMapper.readValue(it, OrderStatusResponse::class.java) }

        if (status?.orderStatus != 2) {
            logger.warn("ArCa finalize: order not paid for orderId=$orderId, orderStatus=${status?.orderStatus}, actionCode=${status?.actionCode}, desc=${status?.actionCodeDescription}")
            return false
        }

        val updated = payment.copy(
            status = PaymentStatus.SUCCESS,
            transactionId = orderId,
            completedAt = Instant.now()
        )
        paymentRepository.save(updated)

        logger.info("ArCa payment completed: orderId=$orderId, orderNumber=${updated.billNo}")

        if (updated.invitationId != null) {
            try {
                invitationService.publishDraftAfterPayment(updated.invitationId, updated.userId)
                logger.info("Invitation auto-published after card payment: invitationId=${updated.invitationId}")
            } catch (e: Exception) {
                logger.error("Failed to auto-publish invitation after card payment: invitationId=${updated.invitationId}", e)
            }
        }

        try {
            referralService.rewardReferrerForPayment(updated.userId)
        } catch (e: Exception) {
            logger.error("Failed to process referral reward for userId=${updated.userId}", e)
        }

        return true
    }

    /**
     * Builds the frontend redirect URL the customer is sent to after returning from the
     * ArCa gateway, e.g.
     * https://host/{lang}/build/confirm?template=...&palette=...&id=...&payment=visa&legal=true&status=success
     * All dynamic params are derived from the invitation tied to the payment.
     */
    fun buildRedirectUrl(orderId: String, success: Boolean): String {
        val status = if (success) "success" else "failed"
        val base = arcaProperties.confirmBaseUrl.trimEnd('/')

        val payment = paymentRepository.findByProviderOrderId(orderId)
        val invitation = payment?.invitationId?.let { invitationRepository.findById(it).orElse(null) }

        val lang = (payment?.language?.takeIf { it.isNotBlank() }
            ?: invitation?.languages?.firstOrNull()
            ?: "en").lowercase()

        val params = buildList {
            invitation?.templateId?.let { add("template" to it) }
            invitation?.colorPaletteId?.let { add("palette" to it) }
            invitation?.id?.let { add("id" to it) }
            add("payment" to "visa")
            add("legal" to "true")
            add("status" to status)
        }.joinToString("&") { (key, value) ->
            "$key=${URLEncoder.encode(value, StandardCharsets.UTF_8)}"
        }

        return "$base/$lang/build/confirm?$params"
    }
}
