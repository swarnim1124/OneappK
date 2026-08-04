package com.xsc.oneapp.feature.timetable.domain.model

/** sch_timetable.tb_tt_entry joined with its tb_tt header. courseId/facId/roomId/
 * timeSlotId are raw IDs - the backend doesn't resolve them to display names (see
 * TimetableNotes.kt); roomId in particular has no master-list endpoint in this
 * module at all (it lives in m_infra). */
data class TimetableEntry(
    val id: String?,
    val ttId: String?,
    val institutionId: String?,
    val academicYearId: String?,
    val termId: String?,
    val programId: String?,
    val semesterId: String?,
    val sectionId: String?,
    val courseId: String?,
    val courseOfferingId: String?,
    val facultyId: String?,
    val facultyCourseAssignmentId: String?,
    val workingDayId: String?,
    val dayOfWeek: String?,
    val timeSlotId: String?,
    val roomId: String?,
    val sessionTypeId: String?,
    val startDate: String?,
    val endDate: String?,
    val isActive: String?,
    val ttCode: String?,
    val ttStatus: String?
)
