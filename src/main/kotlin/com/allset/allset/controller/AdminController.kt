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

    // ── Vendor Categories ──

    @GetMapping("/vendor-categories")
    fun getAllVendorCategories(): List<VendorCategory> = adminService.getAllVendorCategories()

    @GetMapping("/vendor-categories/{id}")
    fun getVendorCategoryById(@PathVariable id: String): VendorCategory = adminService.getVendorCategoryById(id)

    @PostMapping("/vendor-categories")
    fun createVendorCategory(@RequestBody category: VendorCategory): VendorCategory = adminService.createVendorCategory(category)

    @PutMapping("/vendor-categories/{id}")
    fun updateVendorCategory(@PathVariable id: String, @RequestBody category: VendorCategory): VendorCategory = adminService.updateVendorCategory(id, category)

    @DeleteMapping("/vendor-categories/{id}")
    fun deleteVendorCategory(@PathVariable id: String) = adminService.deleteVendorCategory(id)

    // ── Vendors ──

    @GetMapping("/vendors")
    fun getAllVendors(@RequestParam(required = false) categoryId: String?): List<Vendor> = adminService.getAllVendors(categoryId)

    @GetMapping("/vendors/{id}")
    fun getVendorById(@PathVariable id: String): Vendor = adminService.getVendorById(id)

    @PostMapping("/vendors")
    fun createVendor(@RequestBody vendor: Vendor): Vendor = adminService.createVendor(vendor)

    @PutMapping("/vendors/{id}")
    fun updateVendor(@PathVariable id: String, @RequestBody vendor: Vendor): Vendor = adminService.updateVendor(id, vendor)

    @PatchMapping("/vendors/{id}/rating")
    fun updateVendorRating(
        @PathVariable id: String,
        @RequestBody body: Map<String, Double>
    ): Vendor {
        val rating = body["rating"]
            ?: throw IllegalArgumentException("rating is required")
        return adminService.updateVendorRating(id, rating)
    }

    @PatchMapping("/vendors/{id}/status")
    fun updateVendorStatus(
        @PathVariable id: String,
        @RequestBody body: UpdateVendorStatusRequest
    ): Vendor = adminService.updateVendorStatus(id, body.status, body.active)

    @DeleteMapping("/vendors/{id}")
    fun deleteVendor(@PathVariable id: String) = adminService.deleteVendor(id)

    // ── Vendor Subcategories ──

    @GetMapping("/vendor-subcategories")
    fun getAllVendorSubcategories(@RequestParam(required = false) categoryId: String?): List<VendorSubcategory> =
        adminService.getAllVendorSubcategories(categoryId)

    @GetMapping("/vendor-subcategories/{id}")
    fun getVendorSubcategoryById(@PathVariable id: String): VendorSubcategory =
        adminService.getVendorSubcategoryById(id)

    @PostMapping("/vendor-subcategories")
    fun createVendorSubcategory(@RequestBody subcategory: VendorSubcategory): VendorSubcategory =
        adminService.createVendorSubcategory(subcategory)

    @PutMapping("/vendor-subcategories/{id}")
    fun updateVendorSubcategory(@PathVariable id: String, @RequestBody subcategory: VendorSubcategory): VendorSubcategory =
        adminService.updateVendorSubcategory(id, subcategory)

    @DeleteMapping("/vendor-subcategories/{id}")
    fun deleteVendorSubcategory(@PathVariable id: String) = adminService.deleteVendorSubcategory(id)

    // ── Vendor Types ──

    @GetMapping("/vendor-types")
    fun getAllVendorTypes(@RequestParam(required = false) subcategoryId: String?): List<VendorType> =
        adminService.getAllVendorTypes(subcategoryId)

    @GetMapping("/vendor-types/{id}")
    fun getVendorTypeById(@PathVariable id: String): VendorType = adminService.getVendorTypeById(id)

    @PostMapping("/vendor-types")
    fun createVendorType(@RequestBody type: VendorType): VendorType = adminService.createVendorType(type)

    @PutMapping("/vendor-types/{id}")
    fun updateVendorType(@PathVariable id: String, @RequestBody type: VendorType): VendorType =
        adminService.updateVendorType(id, type)

    @DeleteMapping("/vendor-types/{id}")
    fun deleteVendorType(@PathVariable id: String) = adminService.deleteVendorType(id)

    // ── API Error Logs ──

    @GetMapping("/error-logs")
    fun getErrorLogs(
        @RequestParam(required = false) status: Int?,
        @RequestParam(required = false) userId: String?,
        @RequestParam(required = false)
        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        from: java.time.Instant?,
        @RequestParam(required = false)
        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        to: java.time.Instant?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ApiErrorLogPageResponse = adminService.getErrorLogs(status, userId, from, to, page, size)

    @GetMapping("/error-logs/{id}")
    fun getErrorLogById(@PathVariable id: String): ApiErrorLog = adminService.getErrorLogById(id)

    @DeleteMapping("/error-logs/{id}")
    fun deleteErrorLog(@PathVariable id: String) = adminService.deleteErrorLog(id)

    @DeleteMapping("/error-logs")
    fun clearErrorLogs() = adminService.clearErrorLogs()
}

data class UpdateVendorStatusRequest(
    val status: VendorStatus? = null,
    val active: Boolean? = null
)
