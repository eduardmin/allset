package com.allset.allset.controller

import com.allset.allset.dto.*
import com.allset.allset.model.Invitation
import com.allset.allset.repository.DressCodePaletteRepository
import com.allset.allset.service.AuthenticationService
import com.allset.allset.service.ConfirmationService
import com.allset.allset.service.InvitationService
import com.allset.allset.service.TemplateService
import com.allset.allset.service.UserService
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/invitations")
class InvitationController(
    private val invitationService: InvitationService, 
    private val authenticationService: AuthenticationService, 
    private val userService: UserService,
    private val confirmationService: ConfirmationService,
    private val templateService: TemplateService,
    private val dressCodePaletteRepository: DressCodePaletteRepository,
    private val objectMapper: ObjectMapper
) {

    private fun Invitation.toDTOWithPalette(guestCount: Int? = null, template: com.allset.allset.model.Template? = null): InvitationDTO {
        val palette = dressCode?.colorPaletteId?.let { dressCodePaletteRepository.findById(it).orElse(null) }
        return toDTO(guestCount = guestCount, template = template, dressCodePalette = palette)
    }

    private fun parsePartial(body: JsonNode): PartialInvitationDTO =
        objectMapper.treeToValue(body, PartialInvitationDTO::class.java)

    // Top-level keys actually present in the JSON body. Used to distinguish an
    // explicit `null` (clear the field) from an omitted field (keep current value)
    // during partial updates.
    private fun presentFields(body: JsonNode): Set<String> =
        if (body.isObject) body.fieldNames().asSequence().toSet() else emptySet()

    @PostMapping("/draft")
    fun saveDraft(@RequestBody body: JsonNode): InvitationDTO {
        val userId = authenticationService.getCurrentUserId()
        val dto = parsePartial(body)
        return if (dto.id != null) {
            invitationService.patchDraft(dto.id, dto, presentFields(body)).toDTOWithPalette()
        } else {
            val invitation = dto.toNewEntity(userId)
            invitationService.saveDraft(invitation).toDTOWithPalette()
        }
    }

    // Delete draft
    @DeleteMapping("/draft/{id}")
    fun deleteDraft(@PathVariable id: String) {
        invitationService.deleteDraft(id)
    }

    @DeleteMapping("/drafts")
    fun deleteAllDrafts() {
        invitationService.deleteAllDrafts()
    }

    // Delete all draft and active invitations of the current user
    @DeleteMapping("/mine")
    fun deleteAllMyInvitations(): Map<String, Int> {
        val deleted = invitationService.deleteAllUserInvitations()
        return mapOf("deleted" to deleted)
    }

    // Get all drafts
    @GetMapping("/drafts")
    fun getDrafts(): List<InvitationDTO> {
        return invitationService.getDrafts().map { it.toDTOWithPalette() }
    }

    // Get active invitations
    @GetMapping("/active")
    fun getActiveInvitations(): List<InvitationDTO> {
        return invitationService.getActiveInvitations().map { invitation ->
            val guestCount = invitation.id?.let { invitationService.getGuestCount(it) }
            invitation.toDTOWithPalette(guestCount = guestCount)
        }
    }

    // Get expired invitations
    @GetMapping("/expired")
    fun getExpiredInvitations(): List<InvitationDTO> {
        return invitationService.getExpiredInvitations().map { it.toDTOWithPalette() }
    }

    // Publish a draft
    @PostMapping("/{id}/publish")
    fun publishDraft(@PathVariable id: String): InvitationDTO {
        return invitationService.publishDraft(id).toDTOWithPalette()
    }

    @PostMapping
    fun saveInvitation(@RequestBody body: JsonNode): InvitationDTO {
        val userId = authenticationService.getCurrentUserId()
        val dto = parsePartial(body)
        return if (dto.id != null) {
            invitationService.patchInvitation(dto.id, dto, presentFields(body)).toDTOWithPalette()
        } else {
            val invitation = dto.toNewEntity(userId)
            invitationService.createInvitation(invitation).toDTOWithPalette()
        }
    }

    @GetMapping
    fun getInvitations(): List<InvitationDTO> {
        return invitationService.getInvitations().map { it.toDTOWithPalette() }
    }

    @GetMapping("/{id}")
    fun getInvitationById(@PathVariable id: String): InvitationDTO {
        val invitation = invitationService.getInvitationById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found")
        val template = templateService.getTemplateById(invitation.templateId)
        return invitation.toDTOWithPalette(template = template)
    }


    @DeleteMapping("/{id}")
    fun deleteInvitation(@PathVariable id: String) {
        invitationService.deleteInvitation(id)
    }

    @GetMapping("/url/{url}")
    fun getInvitationByUrlExtension(@PathVariable url: String): InvitationDTO {
        val invitation = invitationService.getInvitationByUrlExtension(url)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found")
        val template = templateService.getTemplateById(invitation.templateId)
        return invitation.toDTOWithPalette(template = template)
    }
}
