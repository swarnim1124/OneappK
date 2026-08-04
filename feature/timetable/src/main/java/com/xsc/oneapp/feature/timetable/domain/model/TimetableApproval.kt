package com.xsc.oneapp.feature.timetable.domain.model

/** sch_timetable.tb_tt header record undergoing a status/approval workflow. */
data class TimetableApproval(
    val id: String?,
    val institutionId: String?,
    val academicYearId: String?,
    val termId: String?,
    val sectionId: String?,
    val ttCode: String?,
    val statusId: String?,
    val description: String?
)
