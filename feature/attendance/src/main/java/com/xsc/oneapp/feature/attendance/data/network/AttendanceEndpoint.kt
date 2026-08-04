package com.xsc.oneapp.feature.attendance.data.network

/** Mirrors ATTENDANCE_API_CONTRACT.md (updated contract, verified 2026-07-29), with
 * two routing corrections confirmed 2026-07-31:
 *   1. exceptionApproval has NO view action - it's write-only (update to approve,
 *      delete to revoke) against records surfaced by attendanceException/view. A
 *      view call is rejected with VAL_INVALID_ACTION_TYPE. Never dispatch
 *      EXCEPTION_APPROVAL with ActionTypes.VIEW.
 *   2. condonation lives under sm_shortage, not sm_exception - condonations are
 *      mathematically tied to shortage processing despite reading from the same
 *      tb_att_correction table (and the exact same response shape) as
 *      attendanceException. See AttendanceMapper.kt's shared
 *      toAttendanceCorrectionRequest() for both.
 * All 7 view actions below are wired up (see AttendanceRepositoryImpl.kt);
 * exceptionApproval's write side is not built. */
object AttendanceEndpoint {
    const val MODULE = "m_attendance"

    object SubModules {
        const val CONFIGURATION = "sm_configuration"
        const val RECORDS = "sm_records"
        const val SHORTAGE = "sm_shortage"
        const val EXCEPTION = "sm_exception"
    }

    object Actions {
        const val ATTENDANCE_CONFIGURATION = "attendanceConfiguration"
        const val ATTENDANCE_TYPE = "attendanceType"
        const val ATTENDANCE_EXCEPTION = "attendanceException"
        /** Write-only (update/delete) - never dispatch with ActionTypes.VIEW. */
        const val EXCEPTION_APPROVAL = "exceptionApproval"
        const val ATTENDANCE_RECORD = "attendanceRecord"
        const val ATTENDANCE_SESSION = "attendanceSession"
        const val ATTENDANCE_SUBMISSION = "attendanceSubmission"
        const val ATTENDANCE_SHORTAGE = "attendanceShortage"
        /** Routes under SubModules.SHORTAGE, not SubModules.EXCEPTION. */
        const val CONDONATION = "condonation"
    }

    object ActionTypes {
        const val ADD = "add"
        const val VIEW = "view"
        const val UPDATE = "update"
        const val DELETE = "delete"
        // attendanceConfiguration only:
        const val ACTIVATE = "activate"
        const val DEACTIVATE = "deactivate"
        const val GET_ACTIVE = "get_active"
    }
}
