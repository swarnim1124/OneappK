package com.xsc.oneapp.feature.fee.domain.model

/** A specific student's applied late-penalty record - see FeeNotes.kt for why
 * this is modeled separately from (and doesn't yet cover) the institution-wide
 * penalty policy configuration the write side of this action describes. */
data class FeePenalty(
    val id: String?,
    val studentId: String?,
    val feeAssignmentId: String?,
    val amount: String?,
    val reason: String?,
    val appliedDate: String?,
    val statusId: String?
)
