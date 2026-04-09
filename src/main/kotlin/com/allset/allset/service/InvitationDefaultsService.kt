package com.allset.allset.service

import com.allset.allset.config.LocalizationProperties
import com.allset.allset.model.TemplateDefaultsConfig
import com.allset.allset.repository.TemplateDefaultsRepository
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class InvitationDefaultsService(
    private val messageSource: MessageSource,
    private val localizationProperties: LocalizationProperties,
    private val templateDefaultsRepository: TemplateDefaultsRepository
) {

    fun getDefaultDescription(templateId: String): Map<String, String> {
        val custom = templateDefaultsRepository.findByTemplateId(templateId)
        return custom?.description ?: getLocalizedMessages("invitation.default.description")
    }

    fun getDefaultAgendaTitles(templateId: String): Map<String, Map<String, String>> {
        val custom = templateDefaultsRepository.findByTemplateId(templateId)
        return custom?.agendaTitles ?: mapOf(
            "groom_place" to getLocalizedMessages("agenda.groom_place.title"),
            "church_ceremony" to getLocalizedMessages("agenda.church_ceremony.title"),
            "registration_ceremony" to getLocalizedMessages("agenda.registration_ceremony.title"),
            "bride_place" to getLocalizedMessages("agenda.bride_place.title"),
            "cake_ceremony" to getLocalizedMessages("agenda.cake_ceremony.title"),
            "reception" to getLocalizedMessages("agenda.reception.title"),
            "dinner" to getLocalizedMessages("agenda.dinner.title")
        )
    }

    fun getDefaultDressCodeDescription(templateId: String): Map<String, String> {
        val custom = templateDefaultsRepository.findByTemplateId(templateId)
        return custom?.dressCodeDescription ?: getLocalizedMessages("dresscode.default.description")
    }

    fun getDefaultOurStoryText(templateId: String): Map<String, String> {
        val custom = templateDefaultsRepository.findByTemplateId(templateId)
        return custom?.ourStoryText ?: getLocalizedMessages("ourstory.default.text")
    }

    fun getTemplateDefaults(templateId: String): TemplateDefaultsConfig {
        return TemplateDefaultsConfig(
            templateId = templateId,
            description = getDefaultDescription(templateId),
            agendaTitles = getDefaultAgendaTitles(templateId),
            dressCodeDescription = getDefaultDressCodeDescription(templateId),
            ourStoryText = getDefaultOurStoryText(templateId)
        )
    }

    fun updateTemplateDefaults(templateId: String, update: TemplateDefaultsConfig): TemplateDefaultsConfig {
        val existing = templateDefaultsRepository.findByTemplateId(templateId)
        val toSave = if (existing != null) {
            existing.copy(
                description = update.description ?: existing.description,
                agendaTitles = update.agendaTitles ?: existing.agendaTitles,
                dressCodeDescription = update.dressCodeDescription ?: existing.dressCodeDescription,
                ourStoryText = update.ourStoryText ?: existing.ourStoryText
            )
        } else {
            update.copy(id = null, templateId = templateId)
        }
        return templateDefaultsRepository.save(toSave)
    }

    fun resetTemplateDefaults(templateId: String) {
        val existing = templateDefaultsRepository.findByTemplateId(templateId) ?: return
        templateDefaultsRepository.delete(existing)
    }

    private fun getLocalizedMessages(baseKey: String): Map<String, String> {
        return localizationProperties.supportedLanguages.associateWith { lang ->
            messageSource.getMessage(baseKey, null, Locale.forLanguageTag(lang))
        }
    }
}
