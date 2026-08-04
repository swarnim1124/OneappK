package com.xsc.oneapp.feature.attendance.data.repository

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.xsc.oneapp.feature.attendance.data.network.AttendanceEndpoint
import com.xsc.sdk.auth.SessionManager
import com.xsc.sdk.network.APIClient
import com.xsc.sdk.network.APIError
import com.xsc.sdk.network.api.DispatchRequest
import com.xsc.sdk.network.api.DispatchResponse
import com.xsc.sdk.network.internal.DispatcherApi
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.Response

/**
 * getMyShortageReport() reads the student id from SessionManager.getUserId() -
 * exercising this against a session built from a real JWT (rather than a hardcoded
 * id) is a regression guard for the removed DEV_FORCE_USER_ID shim: this must send
 * the signed-in student's real id, not a forced "1".
 */
class AttendanceRepositoryImplTest {

    @Test
    fun `getMyShortageReport sends the real signed-in student id as studId and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        // Real ATTENDANCE_API_CONTRACT.md sm_shortage/attendanceShortage response shape.
        val rows = JsonParser.parseString(
            """[{"stud_id":101,"total_sessions":40,"present_sessions":30,"attendance_percentage":75.0,"min_required_percentage":80.0,"shortage_percentage":5.0,"risk_level":"WARNING","is_shortage":true}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "3"
        every { sessionManager.getInstitutionId() } returns null
        val repository = AttendanceRepositoryImpl(APIClient(dispatcherApi, Gson()), sessionManager)

        val result = repository.getMyShortageReport()

        assertEquals(1, result.size)
        val row = result.first()
        assertEquals("101", row.studentId)
        assertEquals("40", row.totalSessions)
        assertEquals("30", row.presentSessions)
        assertEquals("75.0", row.attendancePercentage)
        assertEquals("80.0", row.minRequiredPercentage)
        assertEquals("5.0", row.shortagePercentage)
        assertEquals("WARNING", row.riskLevel)
        assertEquals("true", row.isShortage)
        assertEquals(3L, requestSlot.captured.payload["studId"])
        assertEquals(AttendanceEndpoint.MODULE, requestSlot.captured.mod)
        assertEquals(AttendanceEndpoint.Actions.ATTENDANCE_SHORTAGE, requestSlot.captured.action)
    }

    @Test
    fun `includes instId in the payload when the session has one`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("[]")))
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "3"
        every { sessionManager.getInstitutionId() } returns 1
        val repository = AttendanceRepositoryImpl(APIClient(dispatcherApi, Gson()), sessionManager)

        repository.getMyShortageReport()

        assertEquals(1, requestSlot.captured.payload["instId"])
    }

    @Test
    fun `omits instId from the payload when the session doesn't have one`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("[]")))
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "3"
        every { sessionManager.getInstitutionId() } returns null
        val repository = AttendanceRepositoryImpl(APIClient(dispatcherApi, Gson()), sessionManager)

        repository.getMyShortageReport()

        assertEquals(false, requestSlot.captured.payload.containsKey("instId"))
    }

    @Test
    fun `no signed-in student throws a business error instead of dispatching`() {
        val dispatcherApi = mockk<DispatcherApi>()
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns null
        val repository = AttendanceRepositoryImpl(APIClient(dispatcherApi, Gson()), sessionManager)

        assertThrows(APIError.BusinessError::class.java) {
            kotlinx.coroutines.runBlocking { repository.getMyShortageReport() }
        }
    }

    private fun repository(dispatcherApi: DispatcherApi): AttendanceRepositoryImpl {
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getInstitutionId() } returns null
        return AttendanceRepositoryImpl(APIClient(dispatcherApi, Gson()), sessionManager)
    }

    @Test
    fun `getAttendanceConfigurations dispatches to sm_configuration attendanceConfiguration view and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":1,"inst_id":101,"policy_name":"Fall 2026 Strict Policy","min_att_percent":75.0,"allow_late_marking":true,"late_marking_window_minutes":15,"auto_lock_after_hours":24,"require_approval":true,"require_med_proof":false,"effective_from":"2026-08-01","effective_to":"2026-12-31","is_active":true}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))

        val result = repository(dispatcherApi).getAttendanceConfigurations()

        assertEquals(1, result.size)
        val config = result.first()
        assertEquals("Fall 2026 Strict Policy", config.policyName)
        assertEquals("75.0", config.minAttendancePercent)
        assertEquals("15", config.lateMarkingWindowMinutes)
        assertEquals(AttendanceEndpoint.SubModules.CONFIGURATION, requestSlot.captured.subMod)
        assertEquals(AttendanceEndpoint.Actions.ATTENDANCE_CONFIGURATION, requestSlot.captured.action)
        assertEquals(AttendanceEndpoint.ActionTypes.VIEW, requestSlot.captured.actionType)
    }

    @Test
    fun `getAttendanceTypes dispatches to sm_configuration attendanceType view and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":701,"lookup_code":"PRESENT","lookup_name":"Present","lookup_val":"true","is_active":true}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))

        val result = repository(dispatcherApi).getAttendanceTypes()

        assertEquals(1, result.size)
        assertEquals("PRESENT", result.first().lookupCode)
        assertEquals("Present", result.first().lookupName)
        assertEquals(AttendanceEndpoint.SubModules.CONFIGURATION, requestSlot.captured.subMod)
        assertEquals(AttendanceEndpoint.Actions.ATTENDANCE_TYPE, requestSlot.captured.action)
    }

    @Test
    fun `getAttendanceSessions dispatches to sm_records attendanceSession view and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":10001,"class_session_id":201001,"session_date":"2026-08-01","marked_by_fac_id":501,"status_id":1,"started_at":"2026-08-01T09:00:00Z","submitted_at":null,"locked_at":null,"remarks":"Lecture 1: Introduction to Calculus"}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))

        val result = repository(dispatcherApi).getAttendanceSessions()

        assertEquals(1, result.size)
        assertEquals("201001", result.first().classSessionId)
        assertEquals("Lecture 1: Introduction to Calculus", result.first().remarks)
        assertEquals(AttendanceEndpoint.SubModules.RECORDS, requestSlot.captured.subMod)
        assertEquals(AttendanceEndpoint.Actions.ATTENDANCE_SESSION, requestSlot.captured.action)
    }

    @Test
    fun `getSubmissionComplianceReport dispatches to sm_records attendanceSubmission view and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":10001,"class_session_id":201001,"session_date":"2026-08-01","marked_by_fac_id":501,"status_id":2,"started_at":"2026-08-01T09:00:00Z","submitted_at":"2026-08-01T10:05:00Z","locked_at":"2026-08-01T10:05:00Z","remarks":"Submitted on time."}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))

        val result = repository(dispatcherApi).getSubmissionComplianceReport()

        assertEquals(1, result.size)
        assertEquals("2", result.first().statusId)
        assertEquals("Submitted on time.", result.first().remarks)
        assertEquals(AttendanceEndpoint.SubModules.RECORDS, requestSlot.captured.subMod)
        assertEquals(AttendanceEndpoint.Actions.ATTENDANCE_SUBMISSION, requestSlot.captured.action)
    }

    @Test
    fun `getAttendanceRecords dispatches to sm_records attendanceRecord view and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":50001,"att_session_id":10001,"stud_id":1001,"att_status_id":701,"marked_at":"2026-08-01T09:15:00Z","remarks":"Arrived on time"}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))

        val result = repository(dispatcherApi).getAttendanceRecords()

        assertEquals(1, result.size)
        assertEquals("1001", result.first().studentId)
        assertEquals("701", result.first().attendanceStatusId)
        assertEquals(AttendanceEndpoint.SubModules.RECORDS, requestSlot.captured.subMod)
        assertEquals(AttendanceEndpoint.Actions.ATTENDANCE_RECORD, requestSlot.captured.action)
    }

    @Test
    fun `getAttendanceExceptions dispatches to sm_exception attendanceException view and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":1,"att_record_id":50001,"old_status_id":702,"new_status_id":704,"requested_by_user_id":1001,"approved_by_user_id":null,"status_id":708,"correction_reason_id":801,"proof_doc_id":null,"requested_at":"2026-08-02T14:00:00Z","approved_at":null,"remarks":"[EXCEPTION] [RANGE: 2026-08-01 to 2026-08-05] Official Inter-University Athletics Championship [DOC: /uploads/sports.pdf]"}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))

        val result = repository(dispatcherApi).getAttendanceExceptions()

        assertEquals(1, result.size)
        assertEquals("50001", result.first().attendanceRecordId)
        assertEquals("801", result.first().correctionReasonId)
        assertEquals(AttendanceEndpoint.SubModules.EXCEPTION, requestSlot.captured.subMod)
        assertEquals(AttendanceEndpoint.Actions.ATTENDANCE_EXCEPTION, requestSlot.captured.action)
    }

    @Test
    fun `getCondonations dispatches to sm_shortage condonation view, not sm_exception, and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":2,"att_record_id":50002,"old_status_id":702,"new_status_id":704,"requested_by_user_id":1002,"approved_by_user_id":405,"status_id":709,"correction_reason_id":802,"proof_doc_id":455,"requested_at":"2026-11-20T10:00:00Z","approved_at":"2026-11-21T09:30:00Z","remarks":"[CONDONATION] Severe medical emergency during exam preparation week"}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))

        val result = repository(dispatcherApi).getCondonations()

        assertEquals(1, result.size)
        assertEquals("405", result.first().approvedByUserId)
        assertEquals("[CONDONATION] Severe medical emergency during exam preparation week", result.first().remarks)
        assertEquals(AttendanceEndpoint.SubModules.SHORTAGE, requestSlot.captured.subMod)
        assertEquals(AttendanceEndpoint.Actions.CONDONATION, requestSlot.captured.action)
    }
}
