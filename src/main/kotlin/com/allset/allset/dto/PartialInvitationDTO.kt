package com.allset.allset.dto

import com.allset.allset.model.Invitation

data class PartialInvitationDTO(
    val id: String? = null,
    val templateId: String? = null,
    val title: Map<String, String>? = null,
    val urlExtension: String? = null,
    val eventDate: String? = null,
    val description: Map<String, String>? = null,
    val mainImages: List<String>? = null,
    val confirmationEnabled: Boolean? = null,
    val timeline: List<TimelineEventDTO>? = null,
    val countDown: Boolean? = null,
    val connectWithUs: ConnectWithUsDTO? = null,
    val dressCode: DressCodeDTO? = null,
    val albumLink: String? = null,
    val eventVenue: EventVenueDTO? = null,
    val ourStory: OurStoryDTO? = null,
    val languages: List<String>? = null,
    val colorPaletteId: String? = null
)

fun PartialInvitationDTO.toNewEntity(ownerId: String) = Invitation(
    templateId = templateId ?: "",
    ownerId = ownerId,
    title = title ?: emptyMap(),
    urlExtension = urlExtension ?: "",
    eventDate = eventDate,
    description = description,
    mainImages = mainImages,
    confirmationEnabled = confirmationEnabled ?: false,
    timeline = timeline?.map { it.toEntity() },
    countDown = countDown ?: false,
    connectWithUs = connectWithUs?.toEntity(),
    dressCode = dressCode?.toEntity(),
    albumLink = albumLink,
    eventVenue = eventVenue?.toEntity(),
    ourStory = ourStory?.toEntity(),
    languages = languages ?: listOf("en"),
    colorPaletteId = colorPaletteId
)

fun Invitation.mergeWithPartialUpdate(update: PartialInvitationDTO): Invitation {
    return this.copy(
        templateId = update.templateId ?: this.templateId,
        title = update.title ?: this.title,
        urlExtension = update.urlExtension ?: this.urlExtension,
        description = update.description ?: this.description,
        mainImages = update.mainImages ?: this.mainImages,
        confirmationEnabled = update.confirmationEnabled ?: this.confirmationEnabled,
        timeline = update.timeline?.map { it.toEntity() } ?: this.timeline,
        countDown = update.countDown ?: this.countDown,
        connectWithUs = update.connectWithUs?.toEntity() ?: this.connectWithUs,
        dressCode = update.dressCode?.toEntity() ?: this.dressCode,
        albumLink = update.albumLink ?: this.albumLink,
        eventVenue = update.eventVenue?.toEntity() ?: this.eventVenue,
        ourStory = update.ourStory?.toEntity() ?: this.ourStory,
        languages = update.languages ?: this.languages,
        colorPaletteId = update.colorPaletteId ?: this.colorPaletteId
    )
}
