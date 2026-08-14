package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §3.1 - exam centre registration and venue capacity. Admin-only:
 * no student-facing view of this exists in the contract (a student sees their venue
 * via [HallTicket], not the centre roster). */
data class ExamCentre(
    val id: String?,
    val centreName: String?,
    val capacity: String?,
    val address: String?,
    val courses: List<String>,
    val status: String?
)
