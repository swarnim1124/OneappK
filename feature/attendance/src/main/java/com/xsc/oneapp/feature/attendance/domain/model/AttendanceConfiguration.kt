package com.xsc.oneapp.feature.attendance.domain.model

/** sch_attendance.tb_att_policy - institutional attendance policy. */
data class AttendanceConfiguration(
    val id: String?,
    val institutionId: String?,
    val policyName: String?,
    val minAttendancePercent: String?,
    val allowLateMarking: String?,
    val lateMarkingWindowMinutes: String?,
    val autoLockAfterHours: String?,
    val requireApproval: String?,
    val requireMedicalProof: String?,
    val effectiveFrom: String?,
    val effectiveTo: String?,
    val isActive: String?
)
