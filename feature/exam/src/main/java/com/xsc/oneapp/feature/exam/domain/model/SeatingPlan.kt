package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §3.4 - seat allocations for an examination venue. Admin-only: the
 * contract has no student-facing "view my seat" action separate from [HallTicket],
 * which already carries `seatNumber`/`venueDetails` for the signed-in student. */
data class SeatingPlan(
    val id: String?,
    val scheduleId: String?,
    val venueId: String?,
    val courseId: String?,
    val studentId: String?,
    val seatNumber: String?,
    val allocationMethod: String?
)
