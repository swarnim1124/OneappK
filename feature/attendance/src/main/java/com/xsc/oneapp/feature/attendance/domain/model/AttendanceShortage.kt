package com.xsc.oneapp.feature.attendance.domain.model

data class AttendanceShortage(
    val studentId: String?,
    val totalSessions: String?,
    val presentSessions: String?,
    val attendancePercentage: String?,
    val minRequiredPercentage: String?,
    val shortagePercentage: String?,
    val riskLevel: String?,
    val isShortage: String?
)
