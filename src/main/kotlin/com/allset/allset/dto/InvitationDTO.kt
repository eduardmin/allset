package com.allset.allset.dto

import com.allset.allset.model.*

data class InvitationDTO(
    val id: String? = null,
    val templateId: String,
    val title: String,
    val urlExtension: String,
    val eventDate: String,
    val description: String,
    val mainImages: List<String>,
    val closingText: String,
    val confirmationEnabled: Boolean = true,
    val timeline: List<TimelineEventDTO>? = null,
    val dressCode: DressCodeDTO? = null,
    val albumLink: String? = null,
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
    val style: String,
    val colorScheme: String
)

data class OurStoryDTO(
    val text: String,
    val photoUrls: List<String>
)

fun InvitationDTO.toEntity(ownerId: String): Invitation {
    return Invitation(
        id = this.id,
        templateId = this.templateId,
        ownerId = ownerId,
        title = this.title,
        urlExtension = this.urlExtension,
        eventDate = this.eventDate,
        description = this.description,
        mainImages = this.mainImages,
        closingText = this.closingText,
        confirmationEnabled = this.confirmationEnabled,
        timeline = this.timeline?.map { it.toEntity() },
        dressCode = this.dressCode?.toEntity(),
        albumLink = this.albumLink,
        ourStory = this.ourStory?.toEntity(),
        connectWithUs = this.connectWithUs?.toEntity()
    )
}

fun Invitation.toDTO(): InvitationDTO {
    return InvitationDTO(
        id = this.id,
        templateId = this.templateId,
        title = this.title,
        urlExtension = this.urlExtension,
        eventDate = this.eventDate,
        description = this.description,
        mainImages = this.mainImages,
        closingText = this.closingText,
        confirmationEnabled = this.confirmationEnabled,
        timeline = this.timeline?.map { it.toDTO() },
        dressCode = this.dressCode?.toDTO(),
        albumLink = this.albumLink,
        ourStory = this.ourStory?.toDTO(),
        connectWithUs = this.connectWithUs?.toDTO()
    )
}

fun TimelineEventDTO.toEntity() = TimelineEvent(time, actionName, venueName, venueLocation)
fun TimelineEvent.toDTO() = TimelineEventDTO(time, actionName, venueName, venueLocation)

fun ConnectWithUsDTO.toEntity() = ConnectWithUs(
    groomName, groomLinks.map { it.toEntity() }, brideName, brideLinks.map { it.toEntity() }, fullImage
)

fun ConnectWithUs.toDTO() = ConnectWithUsDTO(
    groomName, groomLinks.map { it.toDTO() }, brideName, brideLinks.map { it.toDTO() }, fullImage
)

fun LinkDTO.toEntity() = Link(link, icon)
fun Link.toDTO() = LinkDTO(link, icon)

fun DressCodeDTO.toEntity() = DressCode(style, colorScheme)
fun DressCode.toDTO() = DressCodeDTO(style, colorScheme)

fun OurStoryDTO.toEntity() = OurStory(text, photoUrls)
fun OurStory.toDTO() = OurStoryDTO(text, photoUrls)
