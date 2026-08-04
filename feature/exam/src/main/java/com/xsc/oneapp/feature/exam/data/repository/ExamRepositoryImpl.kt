package com.xsc.oneapp.feature.exam.data.repository

import com.google.gson.JsonElement
import com.xsc.oneapp.core.json.JsonRowUtils
import com.xsc.oneapp.feature.exam.data.mapper.toChallengeRevaluation
import com.xsc.oneapp.feature.exam.data.mapper.toExamResult
import com.xsc.oneapp.feature.exam.data.mapper.toExamSchedule
import com.xsc.oneapp.feature.exam.data.mapper.toHallTicket
import com.xsc.oneapp.feature.exam.data.mapper.toRevaluationRequest
import com.xsc.oneapp.feature.exam.data.network.ExamEndpoint
import com.xsc.oneapp.feature.exam.domain.model.ChallengeRevaluation
import com.xsc.oneapp.feature.exam.domain.model.ExamResult
import com.xsc.oneapp.feature.exam.domain.model.ExamSchedule
import com.xsc.oneapp.feature.exam.domain.model.HallTicket
import com.xsc.oneapp.feature.exam.domain.model.RevaluationRequest
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import com.xsc.sdk.auth.SessionManager
import com.xsc.sdk.network.APIClient
import javax.inject.Inject

class ExamRepositoryImpl @Inject constructor(
    private val apiClient: APIClient,
    private val sessionManager: SessionManager
) : ExamRepository {
    /** m_exam contract: sm_schedule/examSchedule/view - omitting scheduleId returns
     * every schedule for the institution. instId is included when the session has
     * one (defensive-optional, same pattern as attendance's shortage view) so
     * accounts predating institutionId capture don't break the call. */
    override suspend fun getExamSchedules(): List<ExamSchedule> {
        val payload = mutableMapOf<String, Any>()
        sessionManager.getInstitutionId()?.let { payload["instId"] = it }

        val data = apiClient.request<JsonElement?>(
            module = ExamEndpoint.MODULE,
            submodule = ExamEndpoint.SubModules.SCHEDULE,
            action = ExamEndpoint.Actions.EXAM_SCHEDULE,
            actionType = ExamEndpoint.ActionTypes.VIEW,
            payload = payload
        )
        return JsonRowUtils.asRows(data).map { it.toExamSchedule() }
    }

    /** m_exam contract: sm_hallTicket/hallTicket/view - studentId is optional for a
     * student caller (the backend overrides it with the authenticated user from the
     * JWT), but is sent explicitly anyway to match this app's existing convention
     * (see attendance's studId) rather than relying solely on server-side override. */
    override suspend fun getHallTicket(scheduleId: String): List<HallTicket> {
        val studentId = sessionManager.getUserId()
        val payload = mutableMapOf<String, Any>(
            "scheduleId" to (scheduleId.toLongOrNull() ?: scheduleId)
        )
        studentId?.let { payload["studentId"] = it.toLongOrNull() ?: it }

        val data = apiClient.request<JsonElement?>(
            module = ExamEndpoint.MODULE,
            submodule = ExamEndpoint.SubModules.HALL_TICKET,
            action = ExamEndpoint.Actions.HALL_TICKET,
            actionType = ExamEndpoint.ActionTypes.VIEW,
            payload = payload
        )
        return JsonRowUtils.asRows(data).map { it.toHallTicket() }
    }

    /** m_exam contract: sm_results/result/view - courseId is confirmed
     * optional/ignored (tb_result only stores per-schedule aggregate GPA/CGPA, no
     * course-level rows), so it's never sent. Scoped to the signed-in student the
     * same way hallTicket is. */
    override suspend fun getMyResults(): List<ExamResult> {
        val studentId = sessionManager.getUserId()
        val payload = mutableMapOf<String, Any>()
        studentId?.let { payload["studentId"] = it.toLongOrNull() ?: it }

        val data = apiClient.request<JsonElement?>(
            module = ExamEndpoint.MODULE,
            submodule = ExamEndpoint.SubModules.RESULTS,
            action = ExamEndpoint.Actions.RESULT,
            actionType = ExamEndpoint.ActionTypes.VIEW,
            payload = payload
        )
        return JsonRowUtils.asRows(data).map { it.toExamResult() }
    }

    /** m_exam contract: sm_revaluation/revaluationRequest/view. */
    override suspend fun getMyRevaluationRequests(): List<RevaluationRequest> {
        val studentId = sessionManager.getUserId()
        val payload = mutableMapOf<String, Any>()
        studentId?.let { payload["studentId"] = it.toLongOrNull() ?: it }

        val data = apiClient.request<JsonElement?>(
            module = ExamEndpoint.MODULE,
            submodule = ExamEndpoint.SubModules.REVALUATION,
            action = ExamEndpoint.Actions.REVALUATION_REQUEST,
            actionType = ExamEndpoint.ActionTypes.VIEW,
            payload = payload
        )
        return JsonRowUtils.asRows(data).map { it.toRevaluationRequest() }
    }

    /** m_exam contract: sm_revaluation/challengeRevaluation/view. */
    override suspend fun getMyChallengeRevaluations(): List<ChallengeRevaluation> {
        val studentId = sessionManager.getUserId()
        val payload = mutableMapOf<String, Any>()
        studentId?.let { payload["studentId"] = it.toLongOrNull() ?: it }

        val data = apiClient.request<JsonElement?>(
            module = ExamEndpoint.MODULE,
            submodule = ExamEndpoint.SubModules.REVALUATION,
            action = ExamEndpoint.Actions.CHALLENGE_REVALUATION,
            actionType = ExamEndpoint.ActionTypes.VIEW,
            payload = payload
        )
        return JsonRowUtils.asRows(data).map { it.toChallengeRevaluation() }
    }
}
