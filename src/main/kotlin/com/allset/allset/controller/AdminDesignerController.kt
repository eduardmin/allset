package com.allset.allset.controller

import com.allset.allset.dto.ProcessPayoutRequest
import com.allset.allset.dto.SubmissionReviewRequest
import com.allset.allset.dto.UpdateCommissionRequest
import com.allset.allset.model.PayoutRequest
import com.allset.allset.model.TemplateSubmission
import com.allset.allset.model.User
import com.allset.allset.service.DesignerAdminService
import org.springframework.web.bind.annotation.*

/**
 * Admin endpoints for the designer programme. Mounted under /admin, so the
 * existing AdminAccessFilter enforces ADMIN role.
 */
@RestController
@RequestMapping("/admin")
class AdminDesignerController(
    private val designerAdminService: DesignerAdminService
) {

    // ---- Designers / applications ----

    @GetMapping("/designers")
    fun designers(): List<User> = designerAdminService.listDesigners()

    @GetMapping("/designer-applications")
    fun applications(): List<User> = designerAdminService.listApplications()

    @PostMapping("/designer-applications/{userId}/approve")
    fun approveApplication(@PathVariable userId: String): User =
        designerAdminService.approveApplication(userId)

    @PostMapping("/designer-applications/{userId}/reject")
    fun rejectApplication(@PathVariable userId: String): User =
        designerAdminService.rejectApplication(userId)

    @PatchMapping("/designers/{userId}/commission")
    fun setCommission(
        @PathVariable userId: String,
        @RequestBody request: UpdateCommissionRequest
    ): User = designerAdminService.setCommissionRate(userId, request.commissionRate)

    // ---- Submissions ----

    @GetMapping("/submissions")
    fun submissions(@RequestParam(required = false) status: String?): List<TemplateSubmission> =
        designerAdminService.listSubmissions(status)

    @GetMapping("/submissions/{id}")
    fun submission(@PathVariable id: String): TemplateSubmission =
        designerAdminService.getSubmission(id)

    @PatchMapping("/submissions/{id}/status")
    fun reviewSubmission(
        @PathVariable id: String,
        @RequestBody request: SubmissionReviewRequest
    ): TemplateSubmission = designerAdminService.reviewSubmission(id, request)

    // ---- Payouts ----

    @GetMapping("/payouts")
    fun payouts(@RequestParam(required = false) status: String?): List<PayoutRequest> =
        designerAdminService.listPayouts(status)

    @PatchMapping("/payouts/{id}")
    fun processPayout(
        @PathVariable id: String,
        @RequestBody request: ProcessPayoutRequest
    ): PayoutRequest = designerAdminService.processPayout(id, request)
}
