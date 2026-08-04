package com.xsc.oneapp.feature.attendance.data.repository

import com.google.gson.JsonElement
import com.xsc.oneapp.core.json.JsonRowUtils
import com.xsc.oneapp.feature.attendance.data.mapper.toAttendanceConfiguration
import com.xsc.oneapp.feature.attendance.data.mapper.toAttendanceCorrectionRequest
import com.xsc.oneapp.feature.attendance.data.mapper.toAttendanceRecord
import com.xsc.oneapp.feature.attendance.data.mapper.toAttendanceSession
import com.xsc.oneapp.feature.attendance.data.mapper.toAttendanceShortage
import com.xsc.oneapp.feature.attendance.data.mapper.toAttendanceType
import com.xsc.oneapp.feature.attendance.data.network.AttendanceEndpoint
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceConfiguration
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceCorrectionRequest
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceRecord
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceSession
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceShortage
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceType
import com.xsc.oneapp.feature.attendance.domain.repository.AttendanceRepository
import com.xsc.sdk.auth.SessionManager
import com.xsc.sdk.network.APIClient
import com.xsc.sdk.network.APIError
import javax.inject.Inject

class AttendanceRepositoryImpl @Inject constructor(
    private val apiClient: APIClient,
    private val sessionManager: SessionManager
) : AttendanceRepository {
    /** ATTENDANCE_API_CONTRACT.md sm_shortage/attendanceShortage/view - the risk/shortage
     * report for the signed-in student across all their sessions (classSessionId is
     * omitted deliberately - including it would narrow this to one session instead of
     * "my full report"). */
    override suspend fun getMyShortageReport(): List<AttendanceShortage> {
        val studentId = sessionManager.getUserId()
            ?: throw APIError.BusinessError("NO_ID", "No signed-in student to look up attendance for")

        // Contract shows studId/instId as bare JSON numbers, not strings. instId comes
        // from the login response (TokenManager/SessionManager.getInstitutionId()) - it's
        // only included when we actually have one, same defensive-optional pattern used
        // elsewhere in this app, so accounts predating this field don't break the call.
        val payload = mutableMapOf<String, Any>("studId" to (studentId.toLongOrNull() ?: studentId))
        sessionManager.getInstitutionId()?.let { payload["instId"] = it }

        val data = apiClient.request<JsonElement?>(
            module = AttendanceEndpoint.MODULE,
            submodule = AttendanceEndpoint.SubModules.SHORTAGE,
            action = AttendanceEndpoint.Actions.ATTENDANCE_SHORTAGE,
            actionType = AttendanceEndpoint.ActionTypes.VIEW,
            payload = payload
        )
        return JsonRowUtils.asRows(data).map { it.toAttendanceShortage() }
    }

    /** No request-payload example was confirmed for any of the 6 calls below -
     * only the response shapes were (2026-07-31). As a defensive-optional
     * inference (not a confirmed requirement), inst_id is sent when the session
     * has one - same "doesn't hurt if unused, helps if required" reasoning as
     * timetable's instIdPayload(). No other filter is guessed. */
    private fun instIdPayload(): MutableMap<String, Any> {
        val payload = mutableMapOf<String, Any>()
        sessionManager.getInstitutionId()?.let { payload["inst_id"] = it }
        return payload
    }

    /** m_attendance contract: sm_configuration/attendanceConfiguration/view. */
    override suspend fun getAttendanceConfigurations(): List<AttendanceConfiguration> {
        val data = apiClient.request<JsonElement?>(
            module = AttendanceEndpoint.MODULE,
            submodule = AttendanceEndpoint.SubModules.CONFIGURATION,
            action = AttendanceEndpoint.Actions.ATTENDANCE_CONFIGURATION,
            actionType = AttendanceEndpoint.ActionTypes.VIEW,
            payload = instIdPayload()
        )
        return JsonRowUtils.asRows(data).map { it.toAttendanceConfiguration() }
    }

    /** m_attendance contract: sm_configuration/attendanceType/view. */
    override suspend fun getAttendanceTypes(): List<AttendanceType> {
        val data = apiClient.request<JsonElement?>(
            module = AttendanceEndpoint.MODULE,
            submodule = AttendanceEndpoint.SubModules.CONFIGURATION,
            action = AttendanceEndpoint.Actions.ATTENDANCE_TYPE,
            actionType = AttendanceEndpoint.ActionTypes.VIEW,
            payload = instIdPayload()
        )
        return JsonRowUtils.asRows(data).map { it.toAttendanceType() }
    }

    /** m_attendance contract: sm_records/attendanceSession/view. */
    override suspend fun getAttendanceSessions(): List<AttendanceSession> {
        val data = apiClient.request<JsonElement?>(
            module = AttendanceEndpoint.MODULE,
            submodule = AttendanceEndpoint.SubModules.RECORDS,
            action = AttendanceEndpoint.Actions.ATTENDANCE_SESSION,
            actionType = AttendanceEndpoint.ActionTypes.VIEW,
            payload = instIdPayload()
        )
        return JsonRowUtils.asRows(data).map { it.toAttendanceSession() }
    }

    /** m_attendance contract: sm_records/attendanceSubmission/view - same
     * underlying table/shape as attendanceSession (see AttendanceSession's doc
     * comment), framed as a compliance report. */
    override suspend fun getSubmissionComplianceReport(): List<AttendanceSession> {
        val data = apiClient.request<JsonElement?>(
            module = AttendanceEndpoint.MODULE,
            submodule = AttendanceEndpoint.SubModules.RECORDS,
            action = AttendanceEndpoint.Actions.ATTENDANCE_SUBMISSION,
            actionType = AttendanceEndpoint.ActionTypes.VIEW,
            payload = instIdPayload()
        )
        return JsonRowUtils.asRows(data).map { it.toAttendanceSession() }
    }

    /** m_attendance contract: sm_records/attendanceRecord/view. */
    override suspend fun getAttendanceRecords(): List<AttendanceRecord> {
        val data = apiClient.request<JsonElement?>(
            module = AttendanceEndpoint.MODULE,
            submodule = AttendanceEndpoint.SubModules.RECORDS,
            action = AttendanceEndpoint.Actions.ATTENDANCE_RECORD,
            actionType = AttendanceEndpoint.ActionTypes.VIEW,
            payload = instIdPayload()
        )
        return JsonRowUtils.asRows(data).map { it.toAttendanceRecord() }
    }

    /** m_attendance contract: sm_exception/attendanceException/view. */
    override suspend fun getAttendanceExceptions(): List<AttendanceCorrectionRequest> {
        val data = apiClient.request<JsonElement?>(
            module = AttendanceEndpoint.MODULE,
            submodule = AttendanceEndpoint.SubModules.EXCEPTION,
            action = AttendanceEndpoint.Actions.ATTENDANCE_EXCEPTION,
            actionType = AttendanceEndpoint.ActionTypes.VIEW,
            payload = instIdPayload()
        )
        return JsonRowUtils.asRows(data).map { it.toAttendanceCorrectionRequest() }
    }

    /** m_attendance contract: sm_shortage/condonation/view - NOT sm_exception,
     * despite sharing attendanceException's table/shape (confirmed 2026-07-31,
     * see AttendanceEndpoint.kt's doc comment). */
    override suspend fun getCondonations(): List<AttendanceCorrectionRequest> {
        val data = apiClient.request<JsonElement?>(
            module = AttendanceEndpoint.MODULE,
            submodule = AttendanceEndpoint.SubModules.SHORTAGE,
            action = AttendanceEndpoint.Actions.CONDONATION,
            actionType = AttendanceEndpoint.ActionTypes.VIEW,
            payload = instIdPayload()
        )
        return JsonRowUtils.asRows(data).map { it.toAttendanceCorrectionRequest() }
    }
}
