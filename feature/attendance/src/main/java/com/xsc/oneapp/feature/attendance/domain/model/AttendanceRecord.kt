package com.xsc.oneapp.feature.attendance.domain.model

/** sch_attendance.tb_att_record - one student's marking within a session. */
data class AttendanceRecord(
    val id: String?,
    val attendanceSessionId: String?,
    val studentId: String?,
    val attendanceStatusId: String?,
    val markedAt: String?,
    val remarks: String?
)
