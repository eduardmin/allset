package com.allset.allset.dto

import com.allset.allset.model.DesignerApplicationStatus
import com.allset.allset.model.EarningStatus
import com.allset.allset.model.PayoutStatus
import com.allset.allset.model.SubmissionSpec
import com.allset.allset.model.SubmissionStatus
import com.allset.allset.model.UserRole
import java.math.BigDecimal
import java.time.Instant

// ---- Status / profile ----

data class DesignerStatusResponse(
    val role: UserRole,
    val applicationStatus: DesignerApplicationStatus,
    val isDesigner: Boolean
)

data class DesignerApplyRequest(
    val fullName: String,
    val portfolioUrl: String? = null,
    val bio: String? = null
)

data class UpdateDesignerProfileRequest(
    val fullName: String? = null,
    val portfolioUrl: String? = null,
    val bio: String? = null,
    val payoutMethod: String? = null,
    val payoutDetails: String? = null
)

data class DesignerMeResponse(
    val id: String?,
    val email: String,
    val name: String,
    val picture: String?,
    val role: UserRole,
    val applicationStatus: DesignerApplicationStatus,
    val fullName: String?,
    val portfolioUrl: String?,
    val bio: String?,
    val payoutMethod: String?,
    val payoutDetails: String?,
    val commissionRate: Double
)

// ---- Submissions ----

data class CreateSubmissionRequest(
    val title: Map<String, String>? = null,
    val description: Map<String, String>? = null,
    val type: String? = null,
    val figmaUrl: String? = null,
    val coverImage: String? = null,
    val previewImages: List<String>? = null,
    val spec: SubmissionSpec? = null
)

data class UpdateSubmissionRequest(
    val title: Map<String, String>? = null,
    val description: Map<String, String>? = null,
    val type: String? = null,
    val figmaUrl: String? = null,
    val coverImage: String? = null,
    val previewImages: List<String>? = null,
    val spec: SubmissionSpec? = null
)

// Admin review action on a submission.
data class SubmissionReviewRequest(
    // One of: APPROVE, REQUEST_CHANGES, REJECT, START_DEVELOPMENT, RELEASE
    val action: String,
    val message: String? = null,
    val commissionRate: Double? = null,
    val linkedTemplateId: String? = null
)

// ---- Earnings ----

data class EarningsSummaryResponse(
    val lifetime: BigDecimal,
    val pending: BigDecimal,
    val available: BigDecimal,
    val paid: BigDecimal,
    val salesCount: Long,
    val currency: String = "AMD"
)

data class SalesByTemplateResponse(
    val linkedTemplateId: String?,
    val submissionId: String?,
    val title: Map<String, String>,
    val salesCount: Long,
    val totalEarned: BigDecimal
)

// ---- Payouts ----

data class CreatePayoutRequest(
    val amount: BigDecimal,
    val method: String? = null,
    val details: String? = null
)

data class ProcessPayoutRequest(
    // One of: APPROVE, MARK_PAID, REJECT
    val action: String,
    val adminNote: String? = null
)

// Admin sets a designer's default commission rate (0.0 - 1.0).
data class UpdateCommissionRequest(
    val commissionRate: Double
)
