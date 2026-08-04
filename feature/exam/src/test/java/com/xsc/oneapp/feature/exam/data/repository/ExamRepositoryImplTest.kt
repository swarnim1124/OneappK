package com.xsc.oneapp.feature.exam.data.repository

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.xsc.oneapp.feature.exam.data.network.ExamEndpoint
import com.xsc.sdk.auth.SessionManager
import com.xsc.sdk.network.APIClient
import com.xsc.sdk.network.api.DispatchRequest
import com.xsc.sdk.network.api.DispatchResponse
import com.xsc.sdk.network.internal.DispatcherApi
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import retrofit2.Response

/**
 * APIClient.request() is an inline reified function, so it can't be mocked directly -
 * a mock's "request" method is never actually called at the ExamRepositoryImpl call
 * site (the inline body is substituted there instead, hitting the real
 * APIClient.dispatcherApi property). Exercising a real APIClient over a mocked
 * DispatcherApi is what actually intercepts the call.
 */
class ExamRepositoryImplTest {

    @Test
    fun `getExamSchedules dispatches to m_exam sm_schedule examSchedule view and maps rows to domain models`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"exam_name":"Midterm","exam_type":"regular","from_date":"2026-01-01","to_date":"2026-01-10","status":"published"}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getInstitutionId() } returns null

        val result = ExamRepositoryImpl(apiClient, sessionManager).getExamSchedules()

        assertEquals(1, result.size)
        val schedule = result.first()
        assertEquals("Midterm", schedule.name)
        assertEquals("regular", schedule.examType)
        assertEquals("2026-01-01", schedule.fromDate)
        assertEquals("2026-01-10", schedule.toDate)
        assertEquals("published", schedule.status)
        assertEquals(ExamEndpoint.MODULE, requestSlot.captured.mod)
        assertEquals(ExamEndpoint.SubModules.SCHEDULE, requestSlot.captured.subMod)
        assertEquals(ExamEndpoint.Actions.EXAM_SCHEDULE, requestSlot.captured.action)
        assertEquals(ExamEndpoint.ActionTypes.VIEW, requestSlot.captured.actionType)
    }

    @Test
    fun `includes instId in the payload when the session has one`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("[]")))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getInstitutionId() } returns 1

        ExamRepositoryImpl(apiClient, sessionManager).getExamSchedules()

        assertEquals(1, requestSlot.captured.payload["instId"])
    }

    @Test
    fun `omits instId from the payload when the session doesn't have one`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("[]")))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getInstitutionId() } returns null

        ExamRepositoryImpl(apiClient, sessionManager).getExamSchedules()

        assertFalse(requestSlot.captured.payload.containsKey("instId"))
    }

    @Test
    fun `getHallTicket dispatches to m_exam sm_hallTicket hallTicket view and maps rows to domain models`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":12,"schedule_id":45,"stud_id":1002,"venue_details":"Main Campus Hall - Block A, Room 102","seat_number":"S-042","status":"generated"}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "1002"

        val result = ExamRepositoryImpl(apiClient, sessionManager).getHallTicket("45")

        assertEquals(1, result.size)
        val ticket = result.first()
        assertEquals("12", ticket.id)
        assertEquals("45", ticket.scheduleId)
        assertEquals("1002", ticket.studentId)
        assertEquals("Main Campus Hall - Block A, Room 102", ticket.venueDetails)
        assertEquals("S-042", ticket.seatNumber)
        assertEquals("generated", ticket.status)
        assertEquals(45L, requestSlot.captured.payload["scheduleId"])
        assertEquals(1002L, requestSlot.captured.payload["studentId"])
        assertEquals(ExamEndpoint.SubModules.HALL_TICKET, requestSlot.captured.subMod)
        assertEquals(ExamEndpoint.Actions.HALL_TICKET, requestSlot.captured.action)
        assertEquals(ExamEndpoint.ActionTypes.VIEW, requestSlot.captured.actionType)
    }

    @Test
    fun `getHallTicket omits studentId when there's no signed-in user`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("[]")))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns null

        ExamRepositoryImpl(apiClient, sessionManager).getHallTicket("45")

        assertFalse(requestSlot.captured.payload.containsKey("studentId"))
    }

    @Test
    fun `getMyResults dispatches to m_exam sm_results result view and maps rows to domain models`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":1,"schedule_id":10,"stud_id":101,"gpa":8.50,"cgpa":8.30,"result_status":"generated","created_at":"2026-07-31 12:00:00"}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "101"

        val result = ExamRepositoryImpl(apiClient, sessionManager).getMyResults()

        assertEquals(1, result.size)
        val examResult = result.first()
        assertEquals("1", examResult.id)
        assertEquals("10", examResult.scheduleId)
        assertEquals("101", examResult.studentId)
        assertEquals("8.50", examResult.gpa)
        assertEquals("8.30", examResult.cgpa)
        assertEquals("generated", examResult.resultStatus)
        assertEquals("2026-07-31 12:00:00", examResult.createdAt)
        assertEquals(101L, requestSlot.captured.payload["studentId"])
        assertFalse(requestSlot.captured.payload.containsKey("courseId"))
        assertEquals(ExamEndpoint.SubModules.RESULTS, requestSlot.captured.subMod)
        assertEquals(ExamEndpoint.Actions.RESULT, requestSlot.captured.action)
        assertEquals(ExamEndpoint.ActionTypes.VIEW, requestSlot.captured.actionType)
    }

    @Test
    fun `getMyResults omits studentId when there's no signed-in user`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("[]")))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns null

        ExamRepositoryImpl(apiClient, sessionManager).getMyResults()

        assertFalse(requestSlot.captured.payload.containsKey("studentId"))
    }

    @Test
    fun `getMyRevaluationRequests dispatches to m_exam sm_revaluation revaluationRequest view and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":1,"stud_id":101,"course_id":"CS101","schedule_id":10,"reason":"Mark discrepancy","status":"submitted","created_at":"2026-07-31 12:00:00"}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "101"

        val result = ExamRepositoryImpl(apiClient, sessionManager).getMyRevaluationRequests()

        assertEquals(1, result.size)
        val request = result.first()
        assertEquals("1", request.id)
        assertEquals("CS101", request.courseId)
        assertEquals("10", request.scheduleId)
        assertEquals("Mark discrepancy", request.reason)
        assertEquals("submitted", request.status)
        assertEquals(101L, requestSlot.captured.payload["studentId"])
        assertEquals(ExamEndpoint.SubModules.REVALUATION, requestSlot.captured.subMod)
        assertEquals(ExamEndpoint.Actions.REVALUATION_REQUEST, requestSlot.captured.action)
        assertEquals(ExamEndpoint.ActionTypes.VIEW, requestSlot.captured.actionType)
    }

    @Test
    fun `getMyChallengeRevaluations dispatches to m_exam sm_revaluation challengeRevaluation view and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":1,"stud_id":101,"course_id":"CS101","schedule_id":10,"reval_request_id":5,"reason":"Challenge regular reval outcome","status":"submitted","created_at":"2026-07-31 12:00:00"}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "101"

        val result = ExamRepositoryImpl(apiClient, sessionManager).getMyChallengeRevaluations()

        assertEquals(1, result.size)
        val challenge = result.first()
        assertEquals("1", challenge.id)
        assertEquals("5", challenge.revalRequestId)
        assertEquals("Challenge regular reval outcome", challenge.reason)
        assertEquals("submitted", challenge.status)
        assertEquals(ExamEndpoint.SubModules.REVALUATION, requestSlot.captured.subMod)
        assertEquals(ExamEndpoint.Actions.CHALLENGE_REVALUATION, requestSlot.captured.action)
        assertEquals(ExamEndpoint.ActionTypes.VIEW, requestSlot.captured.actionType)
    }
}
