package com.xsc.oneapp.feature.attendance.domain.model

/** sch_attendance.tb_att_correction - shared between attendanceException/view and
 * condonation/view. Both read the same table with the identical response shape;
 * only correction_reason_id/remarks distinguish an exception from a condonation
 * (confirmed 2026-07-31), so this one model backs both repository methods. The
 * date-range and proof-file metadata the UI might want are embedded inside
 * [remarks] as free text (e.g. "[RANGE: ...] ... [DOC: ...]"), not separate
 * fields - no structured parsing of that is attempted here. */
data class AttendanceCorrectionRequest(
    val id: String?,
    val attendanceRecordId: String?,
    val oldStatusId: String?,
    val newStatusId: String?,
    val requestedByUserId: String?,
    val approvedByUserId: String?,
    val statusId: String?,
    val correctionReasonId: String?,
    val proofDocId: String?,
    val requestedAt: String?,
    val approvedAt: String?,
    val remarks: String?
)
