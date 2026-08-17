package com.xsc.oneapp.feature.curriculum.data.repository

import com.google.gson.JsonElement
import com.xsc.oneapp.core.json.JsonRowUtils
import com.xsc.oneapp.feature.curriculum.data.mapper.toCourse
import com.xsc.oneapp.feature.curriculum.data.mapper.toProgramme
import com.xsc.oneapp.feature.curriculum.data.mapper.toSyllabus
import com.xsc.oneapp.feature.curriculum.data.network.CurriculumEndpoint
import com.xsc.oneapp.feature.curriculum.domain.model.Course
import com.xsc.oneapp.feature.curriculum.domain.model.Programme
import com.xsc.oneapp.feature.curriculum.domain.model.Syllabus
import com.xsc.oneapp.feature.curriculum.domain.repository.CurriculumRepository
import com.xsc.sdk.auth.SessionManager
import com.xsc.sdk.network.APIClient
import javax.inject.Inject

/**
 * Every call here used to send `payload = emptyMap()`.
 *
 * OneApp is multi-tenant and resolves the institution server side from the JWT, but
 * every other module in this app (m_fees, m_timetable, m_exam, m_attendance) still
 * sends `inst_id` explicitly, and their handlers filter on it. m_curriculum is the
 * only module that was sending nothing at all - which on a backend that expects the
 * tenant filter reads as "no scope", and comes back as an empty list or a validation
 * error rather than this institution's programmes. That is the "curriculum isn't
 * wired properly" symptom: the screen, ViewModel, use cases and mappers were all
 * correct and the request had no scope on it.
 *
 * `inst_id` is sent defensively-optional - only when the session actually has one, so
 * a backend that ignores the field behaves exactly as before.
 */
class CurriculumRepositoryImpl @Inject constructor(
    private val apiClient: APIClient,
    private val sessionManager: SessionManager
) : CurriculumRepository {

    private fun scopedPayload(): Map<String, Any> {
        val payload = mutableMapOf<String, Any>()
        sessionManager.getInstitutionId()?.let { payload["inst_id"] = it }
        return payload
    }

    private suspend fun view(submodule: String, action: String): JsonElement? =
        apiClient.request(
            module = CurriculumEndpoint.MODULE,
            submodule = submodule,
            action = action,
            actionType = CurriculumEndpoint.ActionTypes.VIEW,
            payload = scopedPayload()
        )

    /** m_curriculum contract: sm_programme/programme/view. */
    override suspend fun getProgrammes(): List<Programme> {
        val data = view(CurriculumEndpoint.SubModules.PROGRAMME, CurriculumEndpoint.Actions.PROGRAMME)
        return JsonRowUtils.asRows(data).map { it.toProgramme() }
    }

    /** m_curriculum contract: sm_course/courseDefinition/view. */
    override suspend fun getCourses(): List<Course> {
        val data = view(
            CurriculumEndpoint.SubModules.COURSE,
            CurriculumEndpoint.Actions.COURSE_DEFINITION
        )
        return JsonRowUtils.asRows(data).map { it.toCourse() }
    }

    /** m_curriculum contract: sm_curriculum/curriculumManagement/view (the
     * "syllabus" entity). Terms are nested inside this action's payload as a
     * "terms" array rather than being separately viewable - the response shape
     * for this "view" call (whether it surfaces the nested terms too) isn't
     * confirmed yet, so [Syllabus] only maps the fields already verified. */
    override suspend fun getSyllabus(): List<Syllabus> {
        val data = view(
            CurriculumEndpoint.SubModules.CURRICULUM,
            CurriculumEndpoint.Actions.CURRICULUM_MANAGEMENT
        )
        return JsonRowUtils.asRows(data).map { it.toSyllabus() }
    }
}
