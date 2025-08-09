package com.allset.allset.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "invitations")
data class Invitation(
    @Id val id: String? = null,
    val templateId: String,
    val ownerId: String,
    val title: Map<String, String>,
    val urlExtension: String,
    val eventDate: String,
    val description: Map<String, String>,
    val mainImages: List<String>,
    val closingText: Map<String, String>,
    val confirmationEnabled: Boolean = true,
    val timeline: List<TimelineEvent>? = null,
    val dressCode: DressCode? = null,
    val albumLink: String? = null,
    val ourStory: OurStory? = null,
    val connectWithUs: ConnectWithUs? = null,
    val languages: List<String> = listOf("en")
)

data class TimelineEvent(
    val time: String,
    val actionName: Map<String, String>,
    val venueName: Map<String, String>,
    val venueLocation: String? = null
)

data class ConnectWithUs(
    val groomName: String,
    val groomLinks: List<Link>,
    val brideName: String,
    val brideLinks: List<Link>,
    val fullImage: String
)

data class Link(
    val link: String,
    val icon: String
)

data class DressCode(
    val style: String, // FORMAL_BLACK_TIE, GARDEN_PARTY_OUTDOOR_CHIC, CASUAL_ELEGANT
    val colorScheme: String // LIGHT_BLUE_PURPLE, etc.
)

data class OurStory(
    val text: Map<String, String>,
    val photoUrls: List<String> // two photos
)
