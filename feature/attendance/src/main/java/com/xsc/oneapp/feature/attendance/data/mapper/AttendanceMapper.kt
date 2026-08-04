package com.xsc.oneapp.feature.attendance.data.mapper

import com.google.gson.JsonObject
import com.xsc.oneapp.core.json.JsonRowUtils
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceConfiguration
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceCorrectionRequest
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceRecord
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceSession
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceShortage
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceType

/** ATTENDANCE_API_CONTRACT.md sm_shortage/attendanceShortage response shape (verified
 * against a real example): one row per student's overall shortage evaluation, not
 * per-class - there is no class/session identifier in the response. */
fun JsonObject.toAttendanceShortage(): AttendanceShortage = AttendanceShortage(
    studentId = JsonRowUtils.firstString(this, "stud_id", "studId"),
    totalSessions = JsonRowUtils.firstString(this, "total_sessions", "totalSessions"),
    presentSessions = JsonRowUtils.firstString(this, "present_sessions", "presentSessions"),
    attendancePercentage = JsonRowUtils.firstString(this, "attendance_percentage", "attendancePercentage"),
    minRequiredPercentage = JsonRowUtils.firstString(this, "min_required_percentage", "minRequiredPercentage"),
    shortagePercentage = JsonRowUtils.firstString(this, "shortage_percentage", "shortagePercentage"),
    riskLevel = JsonRowUtils.firstString(this, "risk_level", "riskLevel"),
    isShortage = JsonRowUtils.firstString(this, "is_shortage", "isShortage")
)

/** sm_configuration/attendanceConfiguration/view field names verified against a
 * real response example (2026-07-31). */
fun JsonObject.toAttendanceConfiguration(): AttendanceConfiguration = AttendanceConfiguration(
    id = JsonRowUtils.firstString(this, "id"),
    institutionId = JsonRowUtils.firstString(this, "inst_id", "instId"),
    policyName = JsonRowUtils.firstString(this, "policy_name", "policyName"),
    minAttendancePercent = JsonRowUtils.firstString(this, "min_att_percent", "minAttPercent"),
    allowLateMarking = JsonRowUtils.firstString(this, "allow_late_marking", "allowLateMarking"),
    lateMarkingWindowMinutes = JsonRowUtils.firstString(this, "late_marking_window_minutes", "lateMarkingWindowMinutes"),
    autoLockAfterHours = JsonRowUtils.firstString(this, "auto_lock_after_hours", "autoLockAfterHours"),
    requireApproval = JsonRowUtils.firstString(this, "require_approval", "requireApproval"),
    requireMedicalProof = JsonRowUtils.firstString(this, "require_med_proof", "requireMedProof"),
    effectiveFrom = JsonRowUtils.firstString(this, "effective_from", "effectiveFrom"),
    effectiveTo = JsonRowUtils.firstString(this, "effective_to", "effectiveTo"),
    isActive = JsonRowUtils.firstString(this, "is_active", "isActive")
)

/** sm_configuration/attendanceType/view field names verified against a real
 * response example (2026-07-31). */
fun JsonObject.toAttendanceType(): AttendanceType = AttendanceType(
    id = JsonRowUtils.firstString(this, "id"),
    lookupCode = JsonRowUtils.firstString(this, "lookup_code", "lookupCode"),
    lookupName = JsonRowUtils.firstString(this, "lookup_name", "lookupName"),
    lookupValue = JsonRowUtils.firstString(this, "lookup_val", "lookupVal"),
    isActive = JsonRowUtils.firstString(this, "is_active", "isActive")
)

/** sm_records/attendanceSession/view AND sm_records/attendanceSubmission/view -
 * identical response shape (see AttendanceSession's doc comment), field names
 * verified against real response examples for both (2026-07-31). */
fun JsonObject.toAttendanceSession(): AttendanceSession = AttendanceSession(
    id = JsonRowUtils.firstString(this, "id"),
    classSessionId = JsonRowUtils.firstString(this, "class_session_id", "classSessionId"),
    sessionDate = JsonRowUtils.firstString(this, "session_date", "sessionDate"),
    markedByFacultyId = JsonRowUtils.firstString(this, "marked_by_fac_id", "markedByFacId"),
    statusId = JsonRowUtils.firstString(this, "status_id", "statusId"),
    startedAt = JsonRowUtils.firstString(this, "started_at", "startedAt"),
    submittedAt = JsonRowUtils.firstString(this, "submitted_at", "submittedAt"),
    lockedAt = JsonRowUtils.firstString(this, "locked_at", "lockedAt"),
    remarks = JsonRowUtils.firstString(this, "remarks")
)

/** sm_records/attendanceRecord/view field names verified against a real response
 * example (2026-07-31). */
fun JsonObject.toAttendanceRecord(): AttendanceRecord = AttendanceRecord(
    id = JsonRowUtils.firstString(this, "id"),
    attendanceSessionId = JsonRowUtils.firstString(this, "att_session_id", "attSessionId"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studId"),
    attendanceStatusId = JsonRowUtils.firstString(this, "att_status_id", "attStatusId"),
    markedAt = JsonRowUtils.firstString(this, "marked_at", "markedAt"),
    remarks = JsonRowUtils.firstString(this, "remarks")
)

/** sm_exception/attendanceException/view AND sm_shortage/condonation/view -
 * identical response shape (see AttendanceCorrectionRequest's doc comment), field
 * names verified against real response examples for both (2026-07-31). */
fun JsonObject.toAttendanceCorrectionRequest(): AttendanceCorrectionRequest = AttendanceCorrectionRequest(
    id = JsonRowUtils.firstString(this, "id"),
    attendanceRecordId = JsonRowUtils.firstString(this, "att_record_id", "attRecordId"),
    oldStatusId = JsonRowUtils.firstString(this, "old_status_id", "oldStatusId"),
    newStatusId = JsonRowUtils.firstString(this, "new_status_id", "newStatusId"),
    requestedByUserId = JsonRowUtils.firstString(this, "requested_by_user_id", "requestedByUserId"),
    approvedByUserId = JsonRowUtils.firstString(this, "approved_by_user_id", "approvedByUserId"),
    statusId = JsonRowUtils.firstString(this, "status_id", "statusId"),
    correctionReasonId = JsonRowUtils.firstString(this, "correction_reason_id", "correctionReasonId"),
    proofDocId = JsonRowUtils.firstString(this, "proof_doc_id", "proofDocId"),
    requestedAt = JsonRowUtils.firstString(this, "requested_at", "requestedAt"),
    approvedAt = JsonRowUtils.firstString(this, "approved_at", "approvedAt"),
    remarks = JsonRowUtils.firstString(this, "remarks")
)
