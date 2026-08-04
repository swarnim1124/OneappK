package com.xsc.oneapp.feature.exam.domain.model

/** A challenge against a completed regular revaluation's outcome - see
 * [RevaluationRequest] for the original request this points back to via
 * [revalRequestId]. */
data class ChallengeRevaluation(
    val id: String?,
    val studentId: String?,
    val courseId: String?,
    val scheduleId: String?,
    val revalRequestId: String?,
    val reason: String?,
    val status: String?,
    val createdAt: String?
)
