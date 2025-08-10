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
    val confirmationEnabled: Boolean,
    val timeline: List<TimelineEvent>? = null,
    val countDown: Boolean,
    val connectWithUs: ConnectWithUs? = null,
    val dressCode: DressCode? = null,
    val albumLink: String? = null,
    val ourStory: OurStory? = null,
    val languages: List<String> = listOf("en"),
    val colorPalette: ColorPalette? = null,
)

data class TimelineEvent(
    val time: String,
    val venueName: Map<String, String>,
    val venueLocation: String? = null
)

data class ConnectWithUs(
    val name: String,
    val phone: String,
    val email: String
)

data class DressCode(
    val description: Map<String, String>,
    val style: String,
    val colorPalette: ColorPalette
)

data class OurStory(
    val text: Map<String, String>,
    val photoUrls: List<String>
)
