package com.allset.allset.dto

import com.allset.allset.model.*
import java.math.BigDecimal
import java.time.Instant

data class InvitationDTO(
    val id: String? = null,
    val templateId: String,
    val title: Map<String, String>,
    val urlExtension: String,
    val eventDate: String? = null,
    val description: Map<String, String>? = null,
    val mainImages: List<String>? = null,
    val closingText: Map<String, String>? = null,
    val confirmationEnabled: Boolean,
    val timeline: List<TimelineEventDTO>? = null,
    val countDown: Boolean = false,
    val connectWithUs: ConnectWithUsDTO? = null,
    val dressCode: DressCodeDTO? = null,
    val albumLink: String? = null,
    val ourStory: OurStoryDTO? = null,
    val languages: List<String> = listOf("en"),
    val colorPaletteId: String? = null,
    val status: InvitationStatus = InvitationStatus.DRAFT,
    val createdAt: Instant? = null,
    val publishedAt: Instant? = null,
    val expiresAt: Instant? = null,
    val finalPrice: BigDecimal? = null,
    val guestCount: Int? = null,
    val lastModifiedAt: Instant? = null
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
    val colorPaletteId: String?
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
    colorPaletteId = colorPaletteId,
    finalPrice = finalPrice,
    expiresAt = expiresAt
)

fun Invitation.toDTO(guestCount: Int? = null) = InvitationDTO(
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
    colorPaletteId = colorPaletteId,
    status = status,
    createdAt = createdAt,
    publishedAt = publishedAt,
    expiresAt = expiresAt,
    finalPrice = finalPrice,
    guestCount = guestCount,
    lastModifiedAt = lastModifiedAt
)

fun TimelineEventDTO.toEntity() = TimelineEvent(time, venueName, venueLocation)
fun TimelineEvent.toDTO() = TimelineEventDTO(time, venueName, venueLocation)

fun ConnectWithUsDTO.toEntity() = ConnectWithUs(name, phone, email)
fun ConnectWithUs.toDTO() = ConnectWithUsDTO(name, phone, email)

fun DressCodeDTO.toEntity() = DressCode(description, style, colorPaletteId)
fun DressCode.toDTO() = DressCodeDTO(description, style, colorPaletteId)

fun OurStoryDTO.toEntity() = OurStory(text, photoUrls)
fun OurStory.toDTO() = OurStoryDTO(text, photoUrls)
