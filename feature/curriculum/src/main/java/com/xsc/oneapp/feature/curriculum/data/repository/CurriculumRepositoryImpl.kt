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
import com.xsc.sdk.network.APIClient
import javax.inject.Inject

class CurriculumRepositoryImpl @Inject constructor(
    private val apiClient: APIClient
) : CurriculumRepository {
    /** m_curriculum contract: sm_programme/programme/view. */
    override suspend fun getProgrammes(): List<Programme> {
        val data = apiClient.request<JsonElement?>(
            module = CurriculumEndpoint.MODULE,
            submodule = CurriculumEndpoint.SubModules.PROGRAMME,
            action = CurriculumEndpoint.Actions.PROGRAMME,
            actionType = CurriculumEndpoint.ActionTypes.VIEW,
            payload = emptyMap()
        )
        return JsonRowUtils.asRows(data).map { it.toProgramme() }
    }

    /** m_curriculum contract: sm_course/courseDefinition/view. */
    override suspend fun getCourses(): List<Course> {
        val data = apiClient.request<JsonElement?>(
            module = CurriculumEndpoint.MODULE,
            submodule = CurriculumEndpoint.SubModules.COURSE,
            action = CurriculumEndpoint.Actions.COURSE_DEFINITION,
            actionType = CurriculumEndpoint.ActionTypes.VIEW,
            payload = emptyMap()
        )
        return JsonRowUtils.asRows(data).map { it.toCourse() }
    }

    /** m_curriculum contract: sm_curriculum/curriculumManagement/view (the
     * "syllabus" entity). Terms are nested inside this action's payload as a
     * "terms" array rather than being separately viewable - the response shape
     * for this "view" call (whether it surfaces the nested terms too) isn't
     * confirmed yet, so [Syllabus] only maps the fields already verified. */
    override suspend fun getSyllabus(): List<Syllabus> {
        val data = apiClient.request<JsonElement?>(
            module = CurriculumEndpoint.MODULE,
            submodule = CurriculumEndpoint.SubModules.CURRICULUM,
            action = CurriculumEndpoint.Actions.CURRICULUM_MANAGEMENT,
            actionType = CurriculumEndpoint.ActionTypes.VIEW,
            payload = emptyMap()
        )
        return JsonRowUtils.asRows(data).map { it.toSyllabus() }
    }
}
