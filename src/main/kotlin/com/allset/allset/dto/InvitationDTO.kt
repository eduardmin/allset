package com.allset.allset.dto

import com.allset.allset.model.*

data class InvitationDTO(
    val id: String? = null,
    val templateId: String,
    val title: Map<String, String>,
    val urlExtension: String,
    val eventDate: String,
    val description: Map<String, String>,
    val mainImages: List<String>,
    val closingText: Map<String, String>? = null,
    val confirmationEnabled: Boolean,
    val timeline: List<TimelineEventDTO>? = null,
    val countDown: Boolean,
    val connectWithUs: ConnectWithUsDTO? = null,
    val dressCode: DressCodeDTO? = null,
    val albumLink: String? = null,
    val ourStory: OurStoryDTO? = null,
    val languages: List<String> = listOf("en"),
    val colorPalette: ColorPalette? = null
)

data class TimelineEventDTO(
    val time: String,
    val venueName: Map<String, String>,
    val venueLocation: String? = null
)

data class ConnectWithUsDTO(
    val name: String,
    val phone: String,
    val email: String
)

data class DressCodeDTO(
    val description: Map<String, String>,
    val style: String,
    val colorPalette: ColorPalette
)

data class OurStoryDTO(
    val text: Map<String, String>,
    val photoUrls: List<String>
)

fun InvitationDTO.toEntity(ownerId: String) = Invitation(
    id = id,
    templateId = templateId,
    ownerId = ownerId,
    title = title,
    urlExtension = urlExtension,
    eventDate = eventDate,
    description = description,
    mainImages = mainImages,
    confirmationEnabled = confirmationEnabled,
    timeline = timeline?.map { it.toEntity() },
    countDown = countDown,
    connectWithUs = connectWithUs?.toEntity(),
    dressCode = dressCode?.toEntity(),
    albumLink = albumLink,
    ourStory = ourStory?.toEntity(),
    languages = languages,
    colorPalette = colorPalette
)

fun Invitation.toDTO() = InvitationDTO(
    id = id,
    templateId = templateId,
    title = title,
    urlExtension = urlExtension,
    eventDate = eventDate,
    description = description,
    mainImages = mainImages,
    confirmationEnabled = confirmationEnabled,
    timeline = timeline?.map { it.toDTO() },
    countDown = countDown,
    connectWithUs = connectWithUs?.toDTO(),
    dressCode = dressCode?.toDTO(),
    albumLink = albumLink,
    ourStory = ourStory?.toDTO(),
    languages = languages,
    colorPalette = colorPalette
)

fun TimelineEventDTO.toEntity() = TimelineEvent(time, venueName, venueLocation)
fun TimelineEvent.toDTO() = TimelineEventDTO(time, venueName, venueLocation)

fun ConnectWithUsDTO.toEntity() = ConnectWithUs(name, phone, email)
fun ConnectWithUs.toDTO() = ConnectWithUsDTO(name, phone, email)

fun DressCodeDTO.toEntity() = DressCode(description, style, colorPalette)
fun DressCode.toDTO() = DressCodeDTO(description, style, colorPalette)

fun OurStoryDTO.toEntity() = OurStory(text, photoUrls)
fun OurStory.toDTO() = OurStoryDTO(text, photoUrls)
