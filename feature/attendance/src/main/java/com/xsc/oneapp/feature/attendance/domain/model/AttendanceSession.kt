package com.xsc.oneapp.feature.attendance.domain.model

/** sch_attendance.tb_att_session - shared between attendanceSession/view (raw
 * session metadata) and attendanceSubmission/view (the same table framed as a
 * compliance report). Both actions return the exact same row shape, so this one
 * model backs both repository methods rather than duplicating an identical class
 * under a different name. */
data class AttendanceSession(
    val id: String?,
    val classSessionId: String?,
    val sessionDate: String?,
    val markedByFacultyId: String?,
    val statusId: String?,
    val startedAt: String?,
    val submittedAt: String?,
    val lockedAt: String?,
    val remarks: String?
)
