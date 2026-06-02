package com.allset.allset.controller

import com.allset.allset.dto.*
import com.allset.allset.model.*
import com.allset.allset.service.AdminService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
class AdminController(
    private val adminService: AdminService
) {

    // ── Dashboard ──

    @GetMapping("/dashboard/stats")
    fun getDashboardStats(): AdminDashboardStats {
        return adminService.getDashboardStats()
    }

    // ── Users ──

    @GetMapping("/users")
    fun getAllUsers(): List<AdminUserResponse> {
        return adminService.getAllUsers()
    }

    @GetMapping("/users/{id}")
    fun getUserById(@PathVariable id: String): AdminUserResponse {
        return adminService.getUserById(id)
    }

    @GetMapping("/users/{id}/invitations")
    fun getUserInvitations(@PathVariable id: String): List<AdminInvitationSummary> {
        return adminService.getUserInvitations(id)
    }

    @PatchMapping("/users/{id}/role")
    fun updateUserRole(
        @PathVariable id: String,
        @RequestBody body: Map<String, String>
    ): AdminUserResponse {
        val role = UserRole.valueOf(body["role"]?.uppercase()
            ?: throw IllegalArgumentException("role is required"))
        return adminService.updateUserRole(id, role)
    }

    @DeleteMapping("/users/{id}")
    fun deleteUser(@PathVariable id: String) {
        adminService.deleteUser(id)
    }

    // ── Invitations ──

    @GetMapping("/invitations")
    fun getAllInvitations(
        @RequestParam(required = false) status: InvitationStatus?,
        @RequestParam(required = false) ownerId: String?
    ): List<Invitation> {
        return adminService.getAllInvitations(status, ownerId)
    }

    @GetMapping("/invitations/{id}")
    fun getInvitationById(@PathVariable id: String): Invitation {
        return adminService.getInvitationById(id)
    }

    @GetMapping("/invitations/{id}/confirmations")
    fun getInvitationConfirmations(@PathVariable id: String): List<Confirmation> {
        return adminService.getInvitationConfirmations(id)
    }

    @PatchMapping("/invitations/{id}/status")
    fun updateInvitationStatus(
        @PathVariable id: String,
        @RequestBody body: Map<String, String>
    ): Invitation {
        val status = InvitationStatus.valueOf(body["status"]?.uppercase()
            ?: throw IllegalArgumentException("status is required"))
        return adminService.updateInvitationStatus(id, status)
    }

    @DeleteMapping("/invitations/{id}")
    fun deleteInvitation(@PathVariable id: String) {
        adminService.deleteInvitation(id)
    }

    // ── Promo Codes ──

    @GetMapping("/promo-codes")
    fun getAllPromoCodes(
        @RequestParam(required = false) type: PromoCodeType?,
        @RequestParam(required = false) businessName: String?
    ): List<PromoCode> {
        return when {
            type != null -> adminService.getPromoCodesByType(type)
            businessName != null -> adminService.getPromoCodesByBusinessName(businessName)
            else -> adminService.getAllPromoCodes()
        }
    }

    @GetMapping("/promo-codes/{id}")
    fun getPromoCodeById(@PathVariable id: String): PromoCode {
        return adminService.getPromoCodeById(id)
    }

    @GetMapping("/promo-codes/{id}/usage")
    fun getPromoCodeUsage(@PathVariable id: String): List<PromoCodeUsage> {
        return adminService.getPromoCodeUsage(id)
    }

    @PostMapping("/promo-codes")
    fun createPromoCode(@RequestBody request: CreatePromoCodeRequest): PromoCode {
        return adminService.createPromoCode(request)
    }

    @PatchMapping("/promo-codes/{id}")
    fun updatePromoCode(
        @PathVariable id: String,
        @RequestBody request: UpdatePromoCodeRequest
    ): PromoCode {
        return adminService.updatePromoCode(id, request)
    }

    @DeleteMapping("/promo-codes/{id}")
    fun deletePromoCode(@PathVariable id: String) {
        adminService.deletePromoCode(id)
    }

    // ── Referrals ──

    @GetMapping("/referrals")
    fun getAllReferrals(
        @RequestParam(required = false) referralCode: String?
    ): List<Referral> {
        return adminService.getAllReferrals(referralCode)
    }

    // ── Confirmations ──

    @GetMapping("/confirmations")
    fun getAllConfirmations(): List<Confirmation> {
        return adminService.getAllConfirmations()
    }

    @DeleteMapping("/confirmations/{id}")
    fun deleteConfirmation(@PathVariable id: String) {
        adminService.deleteConfirmation(id)
    }

    // ── Template Pricing ──

    @GetMapping("/templates/pricing")
    fun getAllTemplatePricing(): List<TemplatePricing> {
        return adminService.getAllTemplatePricing()
    }

    @PutMapping("/templates/{templateId}/pricing")
    fun updateTemplatePricing(
        @PathVariable templateId: String,
        @RequestBody body: Map<String, java.math.BigDecimal>
    ): TemplatePricing {
        val basePrice = body["basePrice"]
            ?: throw IllegalArgumentException("basePrice is required")
        return adminService.updateTemplatePricing(templateId, basePrice)
    }

    // ── Template Defaults ──

    @GetMapping("/templates/{templateId}/defaults")
    fun getTemplateDefaults(@PathVariable templateId: String): TemplateDefaultsConfig {
        return adminService.getTemplateDefaults(templateId)
    }

    @PutMapping("/templates/{templateId}/defaults")
    fun updateTemplateDefaults(
        @PathVariable templateId: String,
        @RequestBody body: TemplateDefaultsConfig
    ): TemplateDefaultsConfig {
        return adminService.updateTemplateDefaults(templateId, body)
    }

    @DeleteMapping("/templates/{templateId}/defaults")
    fun resetTemplateDefaults(@PathVariable templateId: String) {
        adminService.resetTemplateDefaults(templateId)
    }

    // ── Dress Code Palettes ──

    @GetMapping("/dress-code-palettes")
    fun getAllDressCodePalettes(): List<DressCodePalette> {
        return adminService.getAllDressCodePalettes()
    }

    @PostMapping("/dress-code-palettes")
    fun createDressCodePalette(@RequestBody palette: DressCodePalette): DressCodePalette {
        return adminService.createDressCodePalette(palette)
    }

    @PatchMapping("/dress-code-palettes/{id}")
    fun updateDressCodePalette(
        @PathVariable id: String,
        @RequestBody palette: DressCodePalette
    ): DressCodePalette {
        return adminService.updateDressCodePalette(id, palette)
    }

    @DeleteMapping("/dress-code-palettes/{id}")
    fun deleteDressCodePalette(@PathVariable id: String) {
        adminService.deleteDressCodePalette(id)
    }

    // ── FAQ ──

    @GetMapping("/faqs")
    fun getAllFaqs(): List<FaqItem> = adminService.getAllFaqs()

    @GetMapping("/faqs/{id}")
    fun getFaqById(@PathVariable id: String): FaqItem = adminService.getFaqById(id)

    @PostMapping("/faqs")
    fun createFaq(@RequestBody faq: FaqItem): FaqItem = adminService.createFaq(faq)

    @PutMapping("/faqs/{id}")
    fun updateFaq(@PathVariable id: String, @RequestBody faq: FaqItem): FaqItem = adminService.updateFaq(id, faq)

    @DeleteMapping("/faqs/{id}")
    fun deleteFaq(@PathVariable id: String) = adminService.deleteFaq(id)

    // ── Feedback ──

    @GetMapping("/feedbacks")
    fun getAllFeedbacks(): List<FeedbackItem> = adminService.getAllFeedbacks()

    @GetMapping("/feedbacks/{id}")
    fun getFeedbackById(@PathVariable id: String): FeedbackItem = adminService.getFeedbackById(id)

    @PostMapping("/feedbacks")
    fun createFeedback(@RequestBody feedback: FeedbackItem): FeedbackItem = adminService.createFeedback(feedback)

    @PutMapping("/feedbacks/{id}")
    fun updateFeedback(@PathVariable id: String, @RequestBody feedback: FeedbackItem): FeedbackItem = adminService.updateFeedback(id, feedback)

    @DeleteMapping("/feedbacks/{id}")
    fun deleteFeedback(@PathVariable id: String) = adminService.deleteFeedback(id)
}
