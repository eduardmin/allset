package com.allset.allset.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "template_submissions")
data class TemplateSubmission(
    @Id val id: String? = null,
    @Indexed val designerId: String,
    val title: Map<String, String> = emptyMap(),
    val description: Map<String, String> = emptyMap(),
    val type: String = "WEDDING",
    val figmaUrl: String? = null,
    val coverImage: String? = null,
    val previewImages: List<String> = emptyList(),
    // Designer's declaration of which spec sections the design covers
    // (mirrors Template feature flags & image counts).
    val spec: SubmissionSpec = SubmissionSpec(),
    val status: SubmissionStatus = SubmissionStatus.DRAFT,
    val reviewNotes: List<ReviewNote> = emptyList(),
    // Share of each sale that goes to the designer once released (set by admin at release).
    val commissionRate: Double? = null,
    // The hardcoded/catalog template id this submission becomes once developed & live.
    val linkedTemplateId: String? = null,
    val createdAt: Instant = Instant.now(),
    val submittedAt: Instant? = null,
    val reviewedAt: Instant? = null,
    val releasedAt: Instant? = null,
    val lastModifiedAt: Instant = Instant.now()
)

data class SubmissionSpec(
    val mainImageMaxCount: Int = 1,
    val albumImageMaxCount: Int = 0,
    val hasConfirmationNote: Boolean = false,
    val hasWishlist: Boolean = false,
    val hasAdditionalInformation: Boolean = false,
    val hasTimeline: Boolean = true,
    val hasDressCode: Boolean = false,
    val hasOurStory: Boolean = false,
    val supportedLanguages: List<String> = listOf("hy", "en", "ru")
)

data class ReviewNote(
    val authorId: String,
    val authorName: String? = null,
    val message: String,
    val fromStatus: SubmissionStatus? = null,
    val toStatus: SubmissionStatus? = null,
    val createdAt: Instant = Instant.now()
)

enum class SubmissionStatus {
    DRAFT,
    SUBMITTED,
    CHANGES_REQUESTED,
    REJECTED,
    APPROVED,
    IN_DEVELOPMENT,
    RELEASED
}
