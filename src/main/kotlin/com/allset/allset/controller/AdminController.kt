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
    fun getAllPromoCodes(): List<PromoCode> {
        return adminService.getAllPromoCodes()
    }

    @GetMapping("/promo-codes/{id}")
    fun getPromoCodeById(@PathVariable id: String): PromoCode {
        return adminService.getPromoCodeById(id)
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

    // ── Confirmations ──

    @GetMapping("/confirmations")
    fun getAllConfirmations(): List<Confirmation> {
        return adminService.getAllConfirmations()
    }

    @DeleteMapping("/confirmations/{id}")
    fun deleteConfirmation(@PathVariable id: String) {
        adminService.deleteConfirmation(id)
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
}
