package com.xsc.oneapp.feature.curriculum.data.repository

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.xsc.oneapp.feature.curriculum.data.network.CurriculumEndpoint
import com.xsc.sdk.network.APIClient
import com.xsc.sdk.network.api.DispatchRequest
import com.xsc.sdk.network.api.DispatchResponse
import com.xsc.sdk.network.internal.DispatcherApi
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

class CurriculumRepositoryImplTest {

    @Test
    fun `getProgrammes dispatches to m_curriculum sm_programme programme view and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString("""[{"prog_name":"B.Tech CSE","prog_code":"CSE"}]""")
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))
        val apiClient = APIClient(dispatcherApi, Gson())

        val result = CurriculumRepositoryImpl(apiClient).getProgrammes()

        assertEquals(1, result.size)
        assertEquals("B.Tech CSE", result.first().name)
        assertEquals("CSE", result.first().code)
        assertEquals(CurriculumEndpoint.MODULE, requestSlot.captured.mod)
        assertEquals(CurriculumEndpoint.SubModules.PROGRAMME, requestSlot.captured.subMod)
        assertEquals(CurriculumEndpoint.Actions.PROGRAMME, requestSlot.captured.action)
        assertEquals(CurriculumEndpoint.ActionTypes.VIEW, requestSlot.captured.actionType)
    }

    @Test
    fun `an empty backend array maps to an empty programme list`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        coEvery { dispatcherApi.dispatch(any()) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("[]")))
        val apiClient = APIClient(dispatcherApi, Gson())

        val result = CurriculumRepositoryImpl(apiClient).getProgrammes()

        assertEquals(0, result.size)
    }

    @Test
    fun `getCourses dispatches to m_curriculum sm_course courseDefinition view and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":402,"crs_code":"CS101","crs_name":"Introduction to Programming","credit_value":4.0,"lecture_hours":3.0,"tutorial_hours":1.0,"practical_hours":0.0}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))
        val apiClient = APIClient(dispatcherApi, Gson())

        val result = CurriculumRepositoryImpl(apiClient).getCourses()

        assertEquals(1, result.size)
        val course = result.first()
        assertEquals("CS101", course.code)
        assertEquals("Introduction to Programming", course.name)
        assertEquals("4.0", course.creditValue)
        assertEquals("3.0", course.lectureHours)
        assertEquals("1.0", course.tutorialHours)
        assertEquals("0.0", course.practicalHours)
        assertEquals(CurriculumEndpoint.SubModules.COURSE, requestSlot.captured.subMod)
        assertEquals(CurriculumEndpoint.Actions.COURSE_DEFINITION, requestSlot.captured.action)
    }

    @Test
    fun `getSyllabus dispatches to m_curriculum sm_curriculum curriculumManagement view and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString("""[{"id":55,"prog_id":1,"curr_name":"B.Tech CS 2026 Syllabus"}]""")
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))
        val apiClient = APIClient(dispatcherApi, Gson())

        val result = CurriculumRepositoryImpl(apiClient).getSyllabus()

        assertEquals(1, result.size)
        val syllabus = result.first()
        assertEquals("1", syllabus.programmeId)
        assertEquals("B.Tech CS 2026 Syllabus", syllabus.name)
        assertEquals(CurriculumEndpoint.SubModules.CURRICULUM, requestSlot.captured.subMod)
        assertEquals(CurriculumEndpoint.Actions.CURRICULUM_MANAGEMENT, requestSlot.captured.action)
    }
}
