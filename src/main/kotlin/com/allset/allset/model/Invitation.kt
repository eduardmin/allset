package com.allset.allset.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "invitations")
data class Invitation(
    @Id val id: String? = null,
    val ownerId: String,
    val title: String,
    val urlExtension: String, // URL for invitation (unique if needed)
    val eventDate: String, // "TBD" allowed if draft
    val description: String,
    val mainImages: List<String>, // multiple images
    val closingText: String,
    val guestCountLimit: Int? = null,
    val confirmationEnabled: Boolean = true,
    val timeline: List<TimelineEvent>? = null,
    val timelineHidden: Boolean = false,
    val countdownEnabled: Boolean = true,
    val dressCode: DressCode? = null,
    val albumLink: String? = null,
    val albumMessage: String? = null,
    val ourStory: OurStory? = null,
    val connectWithUs: ConnectWithUs? = null
)

data class TimelineEvent(
    val time: String,
    val actionName: String,
    val venueName: String,
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
    val text: String,
    val photoUrls: List<String> // two photos
)
