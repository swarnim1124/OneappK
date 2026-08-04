package com.xsc.oneapp.feature.exam.domain.model

data class RevaluationRequest(
    val id: String?,
    val studentId: String?,
    val courseId: String?,
    val scheduleId: String?,
    val reason: String?,
    val status: String?,
    val createdAt: String?
)
