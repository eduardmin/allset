package com.allset.allset.controller

import com.allset.allset.dto.CreatePayoutRequest
import com.allset.allset.dto.CreateSubmissionRequest
import com.allset.allset.dto.DesignerApplyRequest
import com.allset.allset.dto.DesignerMeResponse
import com.allset.allset.dto.DesignerStatusResponse
import com.allset.allset.dto.EarningsSummaryResponse
import com.allset.allset.dto.SalesByTemplateResponse
import com.allset.allset.dto.UpdateDesignerProfileRequest
import com.allset.allset.dto.UpdateSubmissionRequest
import com.allset.allset.model.DesignerEarning
import com.allset.allset.model.PayoutRequest
import com.allset.allset.model.TemplateSubmission
import com.allset.allset.service.DesignerService
import com.allset.allset.service.EarningsService
import com.allset.allset.service.PayoutService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/designer")
class DesignerController(
    private val designerService: DesignerService,
    private val earningsService: EarningsService,
    private val payoutService: PayoutService
) {

    // ---- Status / onboarding (auth-only, NOT designer-gated) ----

    @GetMapping("/status")
    fun status(): DesignerStatusResponse = designerService.getStatus()

    @PostMapping("/apply")
    fun apply(@RequestBody request: DesignerApplyRequest): DesignerStatusResponse =
        designerService.apply(request)

    // ---- Profile (designer-gated) ----

    @GetMapping("/me")
    fun me(): DesignerMeResponse = designerService.getMe()

    @PatchMapping("/me")
    fun updateMe(@RequestBody request: UpdateDesignerProfileRequest): DesignerMeResponse =
        designerService.updateMe(request)

    // ---- Submissions ----

    @GetMapping("/submissions")
    fun listSubmissions(): List<TemplateSubmission> = designerService.listSubmissions()

    @GetMapping("/submissions/{id}")
    fun getSubmission(@PathVariable id: String): TemplateSubmission =
        designerService.getSubmission(id)

    @PostMapping("/submissions")
    fun createSubmission(@RequestBody request: CreateSubmissionRequest): TemplateSubmission =
        designerService.createSubmission(request)

    @PatchMapping("/submissions/{id}")
    fun updateSubmission(
        @PathVariable id: String,
        @RequestBody request: UpdateSubmissionRequest
    ): TemplateSubmission = designerService.updateSubmission(id, request)

    @PostMapping("/submissions/{id}/submit")
    fun submitSubmission(@PathVariable id: String): TemplateSubmission =
        designerService.submitForReview(id)

    @DeleteMapping("/submissions/{id}")
    fun deleteSubmission(@PathVariable id: String) = designerService.deleteSubmission(id)

    // ---- Earnings ----

    @GetMapping("/earnings")
    fun earnings(): List<DesignerEarning> = earningsService.listEarnings()

    @GetMapping("/earnings/summary")
    fun earningsSummary(): EarningsSummaryResponse = earningsService.getSummary()

    @GetMapping("/sales")
    fun sales(): List<SalesByTemplateResponse> = earningsService.salesByTemplate()

    // ---- Payouts ----

    @GetMapping("/payouts")
    fun payouts(): List<PayoutRequest> = payoutService.listMyPayouts()

    @PostMapping("/payouts")
    fun requestPayout(@RequestBody request: CreatePayoutRequest): PayoutRequest =
        payoutService.requestPayout(request)
}
