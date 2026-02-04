package com.allset.allset.service

import com.allset.allset.config.LocalizationProperties
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class InvitationDefaultsService(
    private val messageSource: MessageSource,
    private val localizationProperties: LocalizationProperties
) {

    fun getDefaultDescription(): Map<String, String> {
        return getLocalizedMessages("invitation.default.description")
    }

    fun getDefaultAgendaTitles(): Map<String, Map<String, String>> {
        return mapOf(
            "groom_place" to getLocalizedMessages("agenda.groom_place.title"),
            "church_ceremony" to getLocalizedMessages("agenda.church_ceremony.title"),
            "registration_ceremony" to getLocalizedMessages("agenda.registration_ceremony.title"),
            "bride_place" to getLocalizedMessages("agenda.bride_place.title"),
            "cake_ceremony" to getLocalizedMessages("agenda.cake_ceremony.title"),
            "reception" to getLocalizedMessages("agenda.reception.title"),
            "dinner" to getLocalizedMessages("agenda.dinner.title")
        )
    }

    fun getDefaultDressCodeDescription(): Map<String, String> {
        return getLocalizedMessages("dresscode.default.description")
    }

    fun getDefaultOurStoryText(): Map<String, String> {
        return getLocalizedMessages("ourstory.default.text")
    }

    private fun getLocalizedMessages(baseKey: String): Map<String, String> {
        return localizationProperties.supportedLanguages.associateWith { lang ->
            messageSource.getMessage(baseKey, null, Locale.forLanguageTag(lang))
        }
    }
}
