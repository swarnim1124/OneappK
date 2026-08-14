package com.xsc.oneapp.feature.exam.data.repository

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.xsc.oneapp.feature.exam.data.network.ExamEndpoint
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
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

    @Test
    fun `getMyExamBlocks requires a signed-in student instead of silently defaulting`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns null

        assertThrows(APIError.BusinessError::class.java) {
            kotlinx.coroutines.runBlocking { ExamRepositoryImpl(apiClient, sessionManager).getMyExamBlocks() }
        }
    }

    @Test
    fun `getMyExamBlocks dispatches to sm_hallTicket studentBlock view and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":1,"stud_id":101,"schedule_id":10,"reason":"Fee arrears","blocked_by":"Accounts office","status":"active","created_at":"2026-08-01"}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "101"

        val result = ExamRepositoryImpl(apiClient, sessionManager).getMyExamBlocks("10")

        assertEquals(1, result.size)
        assertEquals("Fee arrears", result.first().reason)
        assertEquals("Accounts office", result.first().blockedBy)
        assertEquals(101L, requestSlot.captured.payload["studentId"])
        assertEquals(10L, requestSlot.captured.payload["scheduleId"])
        assertEquals(ExamEndpoint.SubModules.HALL_TICKET, requestSlot.captured.subMod)
        assertEquals(ExamEndpoint.Actions.STUDENT_BLOCK, requestSlot.captured.action)
    }

    @Test
    fun `submitRevaluationRequest sends studentId, scheduleId, courseId and reason when present`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "101"

        ExamRepositoryImpl(apiClient, sessionManager)
            .submitRevaluationRequest(scheduleId = "10", courseId = "5", reason = "Marks seem wrong")

        assertEquals(101L, requestSlot.captured.payload["studentId"])
        assertEquals(10L, requestSlot.captured.payload["scheduleId"])
        assertEquals(5L, requestSlot.captured.payload["courseId"])
        assertEquals("Marks seem wrong", requestSlot.captured.payload["reason"])
        assertEquals(ExamEndpoint.SubModules.REVALUATION, requestSlot.captured.subMod)
        assertEquals(ExamEndpoint.Actions.REVALUATION_REQUEST, requestSlot.captured.action)
        assertEquals(ExamEndpoint.ActionTypes.ADD, requestSlot.captured.actionType)
    }

    @Test
    fun `submitRevaluationRequest omits a blank reason instead of sending an empty string`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "101"

        ExamRepositoryImpl(apiClient, sessionManager)
            .submitRevaluationRequest(scheduleId = "10", courseId = "5", reason = "  ")

        assertFalse(requestSlot.captured.payload.containsKey("reason"))
    }

    @Test
    fun `submitPhotocopyRequest sends studentId, scheduleId and courseId`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "101"

        ExamRepositoryImpl(apiClient, sessionManager).submitPhotocopyRequest(scheduleId = "10", courseId = "5")

        assertEquals(101L, requestSlot.captured.payload["studentId"])
        assertEquals(10L, requestSlot.captured.payload["scheduleId"])
        assertEquals(5L, requestSlot.captured.payload["courseId"])
        assertEquals(ExamEndpoint.SubModules.REVALUATION, requestSlot.captured.subMod)
        assertEquals(ExamEndpoint.Actions.PHOTOCOPY_REQUEST, requestSlot.captured.action)
    }

    @Test
    fun `submitAppeal sends studentId, referenceId and reason`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "101"

        ExamRepositoryImpl(apiClient, sessionManager).submitAppeal(referenceId = "9", reason = "Unfair outcome")

        assertEquals(101L, requestSlot.captured.payload["studentId"])
        assertEquals(9L, requestSlot.captured.payload["referenceId"])
        assertEquals("Unfair outcome", requestSlot.captured.payload["reason"])
        assertEquals(ExamEndpoint.Actions.APPEAL, requestSlot.captured.action)
    }

    @Test
    fun `submitChallengeRevaluation sends studentId, scheduleId, courseId and the prior revaluation request id`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "101"

        ExamRepositoryImpl(apiClient, sessionManager)
            .submitChallengeRevaluation(scheduleId = "10", courseId = "5", revaluationRequestId = "9")

        assertEquals(101L, requestSlot.captured.payload["studentId"])
        assertEquals(10L, requestSlot.captured.payload["scheduleId"])
        assertEquals(5L, requestSlot.captured.payload["courseId"])
        assertEquals(9L, requestSlot.captured.payload["revaluationRequestId"])
        assertEquals(ExamEndpoint.Actions.CHALLENGE_REVALUATION, requestSlot.captured.action)
        assertEquals(ExamEndpoint.ActionTypes.ADD, requestSlot.captured.actionType)
    }

    @Test
    fun `getSupplementaryExams dispatches to sm_supplementary supplementaryExam view and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":7,"exam_name":"Backlog Nov 2026","from_date":"2026-11-01","to_date":"2026-11-05","status":"scheduled","course_ids":[101,102]}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getInstitutionId() } returns 1

        val result = ExamRepositoryImpl(apiClient, sessionManager).getSupplementaryExams()

        assertEquals(1, result.size)
        assertEquals("Backlog Nov 2026", result.first().name)
        assertEquals(listOf("101", "102"), result.first().courses)
        assertEquals(ExamEndpoint.SubModules.SUPPLEMENTARY, requestSlot.captured.subMod)
        assertEquals(ExamEndpoint.Actions.SUPPLEMENTARY_EXAM, requestSlot.captured.action)
    }

    @Test
    fun `registerForSupplementaryExam sends studentId, supplementaryExamId and courseIds`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "101"

        ExamRepositoryImpl(apiClient, sessionManager)
            .registerForSupplementaryExam(supplementaryExamId = "7", courseIds = listOf("101", "102"))

        assertEquals(101L, requestSlot.captured.payload["studentId"])
        assertEquals(7L, requestSlot.captured.payload["supplementaryExamId"])
        assertEquals(listOf(101L, 102L), requestSlot.captured.payload["courseIds"])
        assertEquals(ExamEndpoint.Actions.SUPPLEMENTARY_REGISTRATION, requestSlot.captured.action)
    }

    @Test
    fun `getReappearExams dispatches to sm_reappear reappearExam view and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":3,"exam_name":"Improvement Dec 2026","exam_start_date":"2026-12-01","exam_end_date":"2026-12-05","registration_start_date":"2026-11-01","registration_end_date":"2026-11-15","status":"scheduled","course_ids":[201]}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getInstitutionId() } returns 1

        val result = ExamRepositoryImpl(apiClient, sessionManager).getReappearExams()

        assertEquals(1, result.size)
        assertEquals("Improvement Dec 2026", result.first().name)
        assertEquals("2026-11-01", result.first().registrationStartDate)
        assertEquals(ExamEndpoint.SubModules.REAPPEAR, requestSlot.captured.subMod)
        assertEquals(ExamEndpoint.Actions.REAPPEAR_EXAM, requestSlot.captured.action)
    }

    @Test
    fun `registerForReappearExam sends studentId, reappearExamId and courseIds`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "101"

        ExamRepositoryImpl(apiClient, sessionManager)
            .registerForReappearExam(reappearExamId = "3", courseIds = listOf("201"))

        assertEquals(101L, requestSlot.captured.payload["studentId"])
        assertEquals(3L, requestSlot.captured.payload["reappearExamId"])
        assertEquals(listOf(201L), requestSlot.captured.payload["courseIds"])
        assertEquals(ExamEndpoint.Actions.REAPPEAR_REGISTRATION, requestSlot.captured.action)
    }

    @Test
    fun `getMyReappearEligibility scopes to the student and omits reappearExamId when not supplied`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":1,"stud_id":101,"course_id":5,"reappear_exam_id":3,"is_eligible":true,"status":"active"}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "101"

        val result = ExamRepositoryImpl(apiClient, sessionManager).getMyReappearEligibility(null)

        assertEquals(true, result.first().isEligible)
        assertFalse(requestSlot.captured.payload.containsKey("reappearExamId"))
        assertEquals(ExamEndpoint.Actions.REAPPEAR_ELIGIBILITY, requestSlot.captured.action)
    }

    @Test
    fun `getMyMalpracticeCases dispatches to sm_malpractice malpracticeCase view and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":1,"stud_id":101,"schedule_id":10,"description":"Unauthorized material","reported_by":"Invigilator A","status":"pending"}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "101"

        val result = ExamRepositoryImpl(apiClient, sessionManager).getMyMalpracticeCases()

        assertEquals(1, result.size)
        assertEquals("Unauthorized material", result.first().description)
        assertEquals(101L, requestSlot.captured.payload["studentId"])
        assertEquals(ExamEndpoint.SubModules.MALPRACTICE, requestSlot.captured.subMod)
        assertEquals(ExamEndpoint.Actions.MALPRACTICE_CASE, requestSlot.captured.action)
    }

    // ==================== Admin surface (representative coverage) ====================

    @Test
    fun `getExamCentres dispatches to sm_examCenter examCentre view and maps every field`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":5,"centre_name":"Central Examination Hall","capacity":250,"address":"123 Anna Salai, Chennai","courses":[101,102],"status":"active"}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getInstitutionId() } returns 1

        val result = ExamRepositoryImpl(apiClient, sessionManager).getExamCentres()

        assertEquals(1, result.size)
        val centre = result.first()
        assertEquals("5", centre.id)
        assertEquals("Central Examination Hall", centre.centreName)
        assertEquals("250", centre.capacity)
        assertEquals("123 Anna Salai, Chennai", centre.address)
        assertEquals(listOf("101", "102"), centre.courses)
        assertEquals("active", centre.status)
        assertEquals(ExamEndpoint.SubModules.EXAM_CENTER, requestSlot.captured.subMod)
        assertEquals(ExamEndpoint.Actions.EXAM_CENTRE, requestSlot.captured.action)
        assertEquals(ExamEndpoint.ActionTypes.VIEW, requestSlot.captured.actionType)
    }

    @Test
    fun `addSeatingPlan sends every id field as a Long, not the original string`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()

        ExamRepositoryImpl(apiClient, sessionManager).addSeatingPlan(
            scheduleId = "10",
            venueId = "3",
            courseId = "5",
            studentId = "101",
            seatNumber = "S-042",
            allocationMethod = "random"
        )

        assertEquals(10L, requestSlot.captured.payload["scheduleId"])
        assertEquals(3L, requestSlot.captured.payload["venueId"])
        assertEquals(5L, requestSlot.captured.payload["courseId"])
        assertEquals(101L, requestSlot.captured.payload["studentId"])
        assertEquals("S-042", requestSlot.captured.payload["seatNumber"])
        assertEquals("random", requestSlot.captured.payload["allocationMethod"])
        assertEquals(ExamEndpoint.SubModules.SEATING, requestSlot.captured.subMod)
        assertEquals(ExamEndpoint.Actions.SEATING_PLAN, requestSlot.captured.action)
        assertEquals(ExamEndpoint.ActionTypes.ADD, requestSlot.captured.actionType)
    }

    @Test
    fun `updateExamCentre sends actionType UPDATE and omits every null optional field`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()

        ExamRepositoryImpl(apiClient, sessionManager).updateExamCentre(
            centreId = "5",
            centreName = "Central Examination Hall - Block B"
        )

        assertEquals(5L, requestSlot.captured.payload["id"])
        assertEquals("Central Examination Hall - Block B", requestSlot.captured.payload["centreName"])
        assertFalse(requestSlot.captured.payload.containsKey("capacity"))
        assertFalse(requestSlot.captured.payload.containsKey("address"))
        assertFalse(requestSlot.captured.payload.containsKey("courses"))
        assertFalse(requestSlot.captured.payload.containsKey("status"))
        assertEquals(ExamEndpoint.SubModules.EXAM_CENTER, requestSlot.captured.subMod)
        assertEquals(ExamEndpoint.Actions.EXAM_CENTRE, requestSlot.captured.action)
        assertEquals(ExamEndpoint.ActionTypes.UPDATE, requestSlot.captured.actionType)
    }

    @Test
    fun `deleteExamCentre sends actionType DELETE with just the id`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()

        ExamRepositoryImpl(apiClient, sessionManager).deleteExamCentre("5")

        assertEquals(mapOf("id" to 5L), requestSlot.captured.payload)
        assertEquals(ExamEndpoint.SubModules.EXAM_CENTER, requestSlot.captured.subMod)
        assertEquals(ExamEndpoint.Actions.EXAM_CENTRE, requestSlot.captured.action)
        assertEquals(ExamEndpoint.ActionTypes.DELETE, requestSlot.captured.actionType)
    }

    @Test
    fun `publishExamSchedule sends the publish actionType on examSchedulePublication`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()

        ExamRepositoryImpl(apiClient, sessionManager).publishExamSchedule("10")

        assertEquals(ExamEndpoint.Actions.EXAM_SCHEDULE_PUBLICATION, requestSlot.captured.action)
        assertEquals(ExamEndpoint.ActionTypes.PUBLISH, requestSlot.captured.actionType)
        assertEquals(10L, requestSlot.captured.payload["scheduleId"])
    }

    @Test
    fun `holdExamSchedule sends the same action as publish but the hold actionType`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()

        ExamRepositoryImpl(apiClient, sessionManager).holdExamSchedule("10")

        assertEquals(ExamEndpoint.Actions.EXAM_SCHEDULE_PUBLICATION, requestSlot.captured.action)
        assertEquals(ExamEndpoint.ActionTypes.HOLD, requestSlot.captured.actionType)
    }

    @Test
    fun `unpublishExamSchedule sends the same action as publish but the unpublish actionType`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()

        ExamRepositoryImpl(apiClient, sessionManager).unpublishExamSchedule("10")

        assertEquals(ExamEndpoint.Actions.EXAM_SCHEDULE_PUBLICATION, requestSlot.captured.action)
        assertEquals(ExamEndpoint.ActionTypes.UNPUBLISH, requestSlot.captured.actionType)
    }

    @Test
    fun `addQuestionPaper resolves setBy from the session, never from a caller-supplied value`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "55"

        ExamRepositoryImpl(apiClient, sessionManager)
            .addQuestionPaper(scheduleId = "10", courseId = "5", content = "Answer all five questions in Section A.")

        assertEquals(55L, requestSlot.captured.payload["setBy"])
        assertEquals(10L, requestSlot.captured.payload["scheduleId"])
        assertEquals(5L, requestSlot.captured.payload["courseId"])
        assertEquals("Answer all five questions in Section A.", requestSlot.captured.payload["content"])
        assertEquals(ExamEndpoint.SubModules.QUESTION_PAPER, requestSlot.captured.subMod)
        assertEquals(ExamEndpoint.Actions.QUESTION_PAPER, requestSlot.captured.action)
        assertEquals(ExamEndpoint.ActionTypes.ADD, requestSlot.captured.actionType)
    }

    @Test
    fun `reportMalpracticeCase resolves reportedBy from the session, never from a caller-supplied value`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "77"

        ExamRepositoryImpl(apiClient, sessionManager)
            .reportMalpracticeCase(studentId = "101", scheduleId = "10", description = "Unauthorized notes found in possession")

        assertEquals(77L, requestSlot.captured.payload["reportedBy"])
        assertEquals(101L, requestSlot.captured.payload["studentId"])
        assertEquals(10L, requestSlot.captured.payload["scheduleId"])
        assertEquals(ExamEndpoint.Actions.MALPRACTICE_CASE, requestSlot.captured.action)
        assertEquals(ExamEndpoint.ActionTypes.ADD, requestSlot.captured.actionType)
    }

    @Test
    fun `approveResults maps the resultApproval add response into a ResultApprovalStatus`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val response = JsonParser.parseString(
            """{"schedule_id":45,"level":"ADMIN","chain_complete":false,"remarks":"Level 1 approval"}"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = response))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()

        val result = ExamRepositoryImpl(apiClient, sessionManager)
            .approveResults(scheduleId = "45", level = "ADMIN", remarks = "Level 1 approval")

        assertEquals("45", result.scheduleId)
        assertEquals("ADMIN", result.level)
        assertEquals(false, result.chainComplete)
        assertEquals("Level 1 approval", result.remarks)
        assertEquals(ExamEndpoint.SubModules.RESULT_APPROVAL, requestSlot.captured.subMod)
        assertEquals(ExamEndpoint.Actions.RESULT_APPROVAL, requestSlot.captured.action)
        assertEquals(ExamEndpoint.ActionTypes.ADD, requestSlot.captured.actionType)
    }

    @Test
    fun `approveResults falls back to the caller's own arguments when the response carries nothing useful`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        coEvery { dispatcherApi.dispatch(any()) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("[]")))
        val apiClient = APIClient(dispatcherApi, Gson())
        val sessionManager = mockk<SessionManager>()

        val result = ExamRepositoryImpl(apiClient, sessionManager)
            .approveResults(scheduleId = "45", level = "ADMIN", remarks = "Final review")

        assertEquals("45", result.scheduleId)
        assertEquals("ADMIN", result.level)
        assertEquals(null, result.chainComplete)
        assertEquals("Final review", result.remarks)
    }
}
