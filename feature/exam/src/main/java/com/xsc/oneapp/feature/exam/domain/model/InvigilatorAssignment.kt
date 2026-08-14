package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §3.5 - invigilator duties and faculty room assignments. Admin-only. */
data class InvigilatorAssignment(
    val id: String?,
    val scheduleId: String?,
    val venueId: String?,
    val facultyId: String?,
    val dutyType: String?,
    val status: String?
)
