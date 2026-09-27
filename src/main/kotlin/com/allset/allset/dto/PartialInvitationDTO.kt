package com.allset.allset.dto

import com.allset.allset.model.Invitation

data class PartialInvitationDTO(
    val id: String? = null,
    val templateId: String? = null,
    val title: Map<String, String>? = null,
    val groomName: Map<String, String>? = null,
    val brideName: Map<String, String>? = null,
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
    val wishlist: WishlistDTO? = null,
    val additionalInformation: List<Map<String, String>>? = null,
    val languages: List<String>? = null,
    val colorPaletteId: String? = null
)

fun PartialInvitationDTO.toNewEntity(ownerId: String) = Invitation(
    templateId = templateId ?: "",
    ownerId = ownerId,
    title = title ?: emptyMap(),
    groomName = groomName,
    brideName = brideName,
    urlExtension = "",
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
    wishlist = wishlist?.toEntity(),
    additionalInformation = additionalInformation,
    languages = languages ?: listOf("en"),
    colorPaletteId = colorPaletteId
)

/**
 * Merges a partial update into an existing invitation.
 *
 * [presentFields] holds the names of the top-level keys that were actually present
 * in the incoming JSON body. This lets us distinguish an explicit `null` (clear the
 * field) from an omitted field (keep the current value) for optional/nullable fields,
 * which is impossible from the deserialized DTO alone (both arrive as `null`).
 *
 * Required, non-null fields (title, templateId, confirmationEnabled, countDown,
 * languages) are never cleared: a `null`/absent value keeps the current one.
 */
fun Invitation.mergeWithPartialUpdate(
    update: PartialInvitationDTO,
    presentFields: Set<String>
): Invitation {
    fun present(field: String) = field in presentFields
    return this.copy(
        templateId = update.templateId ?: this.templateId,
        title = update.title ?: this.title,
        groomName = if (present("groomName")) update.groomName else this.groomName,
        brideName = if (present("brideName")) update.brideName else this.brideName,
        eventDate = if (present("eventDate")) update.eventDate else this.eventDate,
        description = if (present("description")) update.description else this.description,
        mainImages = if (present("mainImages")) update.mainImages else this.mainImages,
        confirmationEnabled = update.confirmationEnabled ?: this.confirmationEnabled,
        timeline = if (present("timeline")) update.timeline?.map { it.toEntity() } else this.timeline,
        countDown = update.countDown ?: this.countDown,
        connectWithUs = if (present("connectWithUs")) update.connectWithUs?.toEntity() else this.connectWithUs,
        dressCode = if (present("dressCode")) update.dressCode?.toEntity() else this.dressCode,
        albumLink = if (present("albumLink")) update.albumLink else this.albumLink,
        eventVenue = if (present("eventVenue")) update.eventVenue?.toEntity() else this.eventVenue,
        ourStory = if (present("ourStory")) update.ourStory?.toEntity() else this.ourStory,
        wishlist = if (present("wishlist")) update.wishlist?.toEntity() else this.wishlist,
        additionalInformation = if (present("additionalInformation")) update.additionalInformation else this.additionalInformation,
        languages = update.languages ?: this.languages,
        colorPaletteId = if (present("colorPaletteId")) update.colorPaletteId else this.colorPaletteId
    )
}
