package com.allset.allset.service

import com.allset.allset.config.LocalizationProperties
import com.allset.allset.dto.*
import com.allset.allset.model.*
import com.allset.allset.repository.ConfirmationRepository
import com.allset.allset.repository.InvitationRepository
import com.allset.allset.repository.PromoCodeRepository
import com.allset.allset.repository.UserRepository
import com.allset.allset.service.InvitationDefaultsService
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class AdminService(
    private val userRepository: UserRepository,
    private val invitationRepository: InvitationRepository,
    private val confirmationRepository: ConfirmationRepository,
    private val promoCodeRepository: PromoCodeRepository,
    private val messageSource: MessageSource,
    private val localizationProperties: LocalizationProperties,
    private val invitationDefaultsService: InvitationDefaultsService,
    private val dressCodePaletteService: DressCodePaletteService
) {

    // ── Dashboard ──

    fun getDashboardStats(): AdminDashboardStats {
        return AdminDashboardStats(
            totalUsers = userRepository.count(),
            totalInvitations = invitationRepository.count(),
            activeInvitations = invitationRepository.countByStatus(InvitationStatus.ACTIVE),
            draftInvitations = invitationRepository.countByStatus(InvitationStatus.DRAFT),
            expiredInvitations = invitationRepository.countByStatus(InvitationStatus.EXPIRED),
            totalConfirmations = confirmationRepository.count(),
            totalPromoCodes = promoCodeRepository.count(),
            activePromoCodes = promoCodeRepository.countByActiveTrue()
        )
    }

    // ── Users ──

    fun getAllUsers(): List<AdminUserResponse> {
        val users = userRepository.findAll()
        return users.map { user ->
            val invitationCount = invitationRepository.findAllByOwnerId(user.id!!).size
            user.toAdminResponse(invitationCount)
        }
    }

    fun getUserById(id: String): AdminUserResponse {
        val user = userRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
        }
        val invitationCount = invitationRepository.findAllByOwnerId(user.id!!).size
        return user.toAdminResponse(invitationCount)
    }

    fun getUserInvitations(userId: String): List<AdminInvitationSummary> {
        userRepository.findById(userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
        }
        val invitations = invitationRepository.findAllByOwnerId(userId)
        return invitations.map { it.toAdminSummary() }
    }

    private fun Invitation.toAdminSummary() = AdminInvitationSummary(
        id = this.id,
        title = this.title,
        templateId = this.templateId,
        templateName = resolveTemplateName(this.templateId),
        status = this.status,
        createdAt = this.createdAt,
        publishedAt = this.publishedAt,
        expiresAt = this.expiresAt
    )

    private fun resolveTemplateName(templateId: String): Map<String, String> {
        return localizationProperties.supportedLanguages.associateWith { lang ->
            try {
                messageSource.getMessage("$templateId.name", null, Locale.forLanguageTag(lang))
            } catch (_: Exception) {
                templateId
            }
        }
    }

    fun updateUserRole(userId: String, role: UserRole): AdminUserResponse {
        val user = userRepository.findById(userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
        }
        val updated = userRepository.save(user.copy(role = role))
        val invitationCount = invitationRepository.findAllByOwnerId(updated.id!!).size
        return updated.toAdminResponse(invitationCount)
    }

    fun deleteUser(userId: String) {
        val user = userRepository.findById(userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
        }
        userRepository.delete(user)
    }

    // ── Invitations ──

    fun getAllInvitations(status: InvitationStatus?, ownerId: String?): List<Invitation> {
        return when {
            status != null && ownerId != null -> invitationRepository.findAllByOwnerIdAndStatus(ownerId, status)
            status != null -> invitationRepository.findAllByStatus(status)
            ownerId != null -> invitationRepository.findAllByOwnerId(ownerId)
            else -> invitationRepository.findAll()
        }
    }

    fun getInvitationById(id: String): Invitation {
        return invitationRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found.")
        }
    }

    fun getInvitationConfirmations(invitationId: String): List<Confirmation> {
        invitationRepository.findById(invitationId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found.")
        }
        return confirmationRepository.findAllByInvitationIdAndDeletedFalse(invitationId)
    }

    fun updateInvitationStatus(invitationId: String, status: InvitationStatus): Invitation {
        val invitation = invitationRepository.findById(invitationId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found.")
        }
        return invitationRepository.save(invitation.copy(status = status))
    }

    fun deleteInvitation(invitationId: String) {
        val invitation = invitationRepository.findById(invitationId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found.")
        }
        invitationRepository.delete(invitation)
    }

    // ── Promo Codes ──

    fun getAllPromoCodes(): List<PromoCode> {
        return promoCodeRepository.findAll()
    }

    fun getPromoCodeById(id: String): PromoCode {
        return promoCodeRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Promo code not found.")
        }
    }

    fun createPromoCode(request: CreatePromoCodeRequest): PromoCode {
        val existing = promoCodeRepository.findByCodeIgnoreCase(request.code)
        if (existing != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Promo code '${request.code}' already exists.")
        }
        val promoCode = PromoCode(
            code = request.code.uppercase(),
            discountType = request.discountType,
            discountValue = request.discountValue,
            active = request.active,
            startsAt = request.startsAt,
            expiresAt = request.expiresAt
        )
        return promoCodeRepository.save(promoCode)
    }

    fun updatePromoCode(id: String, request: UpdatePromoCodeRequest): PromoCode {
        val existing = promoCodeRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Promo code not found.")
        }
        val updated = existing.copy(
            code = request.code?.uppercase() ?: existing.code,
            discountType = request.discountType ?: existing.discountType,
            discountValue = request.discountValue ?: existing.discountValue,
            active = request.active ?: existing.active,
            startsAt = request.startsAt ?: existing.startsAt,
            expiresAt = request.expiresAt ?: existing.expiresAt
        )
        return promoCodeRepository.save(updated)
    }

    fun deletePromoCode(id: String) {
        val promoCode = promoCodeRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Promo code not found.")
        }
        promoCodeRepository.delete(promoCode)
    }

    // ── Confirmations ──

    fun getAllConfirmations(): List<Confirmation> {
        return confirmationRepository.findAll()
    }

    fun deleteConfirmation(id: String) {
        val confirmation = confirmationRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Confirmation not found.")
        }
        confirmationRepository.delete(confirmation)
    }

    // ── Template Defaults ──

    fun getTemplateDefaults(templateId: String): TemplateDefaultsConfig {
        return invitationDefaultsService.getTemplateDefaults(templateId)
    }

    fun updateTemplateDefaults(templateId: String, update: TemplateDefaultsConfig): TemplateDefaultsConfig {
        return invitationDefaultsService.updateTemplateDefaults(templateId, update)
    }

    fun resetTemplateDefaults(templateId: String) {
        invitationDefaultsService.resetTemplateDefaults(templateId)
    }

    // ── Dress Code Palettes ──

    fun getAllDressCodePalettes(): List<DressCodePalette> = dressCodePaletteService.getAll()

    fun createDressCodePalette(palette: DressCodePalette): DressCodePalette = dressCodePaletteService.create(palette)

    fun updateDressCodePalette(id: String, palette: DressCodePalette): DressCodePalette = dressCodePaletteService.update(id, palette)

    fun deleteDressCodePalette(id: String) = dressCodePaletteService.delete(id)
}
