package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §8.1 - a request for a photocopy of an evaluated answer script,
 * normally the step a student takes before deciding whether to apply for revaluation. */
data class PhotocopyRequest(
    val id: String?,
    val studentId: String?,
    val scheduleId: String?,
    val courseId: String?,
    val status: String?,
    val createdAt: String?
)
