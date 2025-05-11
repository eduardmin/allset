package com.allset.allset.dto

import com.allset.allset.model.ConnectWithUs
import com.allset.allset.model.DressCode
import com.allset.allset.model.Invitation
import com.allset.allset.model.Link
import com.allset.allset.model.OurStory
import com.allset.allset.model.TimelineEvent

data class InvitationDTO(
    val id: String? = null,
    val title: String,
    val urlExtension: String,
    val eventDate: String,
    val description: String,
    val mainImages: List<String>,
    val closingText: String,
    val guestCountLimit: Int? = null,
    val confirmationEnabled: Boolean = true,
    val timeline: List<TimelineEventDTO>? = null,
    val timelineHidden: Boolean = false,
    val countdownEnabled: Boolean = true,
    val dressCode: DressCodeDTO? = null,
    val albumLink: String? = null,
    val albumMessage: String? = null,
    val ourStory: OurStoryDTO? = null,
    val connectWithUs: ConnectWithUsDTO? = null
)

data class TimelineEventDTO(
    val time: String,
    val actionName: String,
    val venueName: String,
    val venueLocation: String? = null
)

data class ConnectWithUsDTO(
    val groomName: String,
    val groomLinks: List<LinkDTO>,
    val brideName: String,
    val brideLinks: List<LinkDTO>,
    val fullImage: String
)

data class LinkDTO(
    val link: String,
    val icon: String
)

data class DressCodeDTO(
    val style: String,       // Example: FORMAL_BLACK_TIE
    val colorScheme: String  // Example: LIGHT_BLUE_PURPLE
)

data class OurStoryDTO(
    val text: String,
    val photoUrls: List<String> // Two photos
)


fun InvitationDTO.toEntity(ownerId: String): Invitation {
    return Invitation(
        id = this.id,
        ownerId = ownerId,
        title = this.title,
        urlExtension = this.urlExtension,
        eventDate = this.eventDate,
        description = this.description,
        mainImages = this.mainImages,
        closingText = this.closingText,
        guestCountLimit = this.guestCountLimit,
        confirmationEnabled = this.confirmationEnabled,
        timeline = this.timeline?.map { it.toEntity() },
        timelineHidden = this.timelineHidden,
        countdownEnabled = this.countdownEnabled,
        dressCode = this.dressCode?.toEntity(),
        albumLink = this.albumLink,
        albumMessage = this.albumMessage,
        ourStory = this.ourStory?.toEntity(),
        connectWithUs = this.connectWithUs?.toEntity()
    )
}

fun Invitation.toDTO(): InvitationDTO {
    return InvitationDTO(
        id = this.id,
        title = this.title,
        urlExtension = this.urlExtension,
        eventDate = this.eventDate,
        description = this.description,
        mainImages = this.mainImages,
        closingText = this.closingText,
        guestCountLimit = this.guestCountLimit,
        confirmationEnabled = this.confirmationEnabled,
        timeline = this.timeline?.map { it.toDTO() },
        timelineHidden = this.timelineHidden,
        countdownEnabled = this.countdownEnabled,
        dressCode = this.dressCode?.toDTO(),
        albumLink = this.albumLink,
        albumMessage = this.albumMessage,
        ourStory = this.ourStory?.toDTO(),
        connectWithUs = this.connectWithUs?.toDTO()
    )
}

fun TimelineEventDTO.toEntity(): TimelineEvent {
    return TimelineEvent(
        time = this.time,
        actionName = this.actionName,
        venueName = this.venueName,
        venueLocation = this.venueLocation
    )
}

fun TimelineEvent.toDTO(): TimelineEventDTO {
    return TimelineEventDTO(
        time = this.time,
        actionName = this.actionName,
        venueName = this.venueName,
        venueLocation = this.venueLocation
    )
}

fun ConnectWithUsDTO.toEntity(): ConnectWithUs {
    return ConnectWithUs(
        groomName = this.groomName,
        groomLinks = this.groomLinks.map { it.toEntity() },
        brideName = this.brideName,
        brideLinks = this.brideLinks.map { it.toEntity() },
        fullImage = this.fullImage
    )
}

fun ConnectWithUs.toDTO(): ConnectWithUsDTO {
    return ConnectWithUsDTO(
        groomName = this.groomName,
        groomLinks = this.groomLinks.map { it.toDTO() },
        brideName = this.brideName,
        brideLinks = this.brideLinks.map { it.toDTO() },
        fullImage = this.fullImage
    )
}

fun LinkDTO.toEntity(): Link {
    return Link(
        link = this.link,
        icon = this.icon
    )
}

fun Link.toDTO(): LinkDTO {
    return LinkDTO(
        link = this.link,
        icon = this.icon
    )
}

fun DressCodeDTO.toEntity(): DressCode {
    return DressCode(
        style = this.style,
        colorScheme = this.colorScheme
    )
}

fun DressCode.toDTO(): DressCodeDTO {
    return DressCodeDTO(
        style = this.style,
        colorScheme = this.colorScheme
    )
}

fun OurStoryDTO.toEntity(): OurStory {
    return OurStory(
        text = this.text,
        photoUrls = this.photoUrls
    )
}

fun OurStory.toDTO(): OurStoryDTO {
    return OurStoryDTO(
        text = this.text,
        photoUrls = this.photoUrls
    )
}
