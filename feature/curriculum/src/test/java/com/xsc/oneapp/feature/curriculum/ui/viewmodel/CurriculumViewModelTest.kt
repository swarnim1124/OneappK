package com.xsc.oneapp.feature.curriculum.ui.viewmodel

import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.curriculum.domain.model.Course
import com.xsc.oneapp.feature.curriculum.domain.model.Programme
import com.xsc.oneapp.feature.curriculum.domain.model.Syllabus
import com.xsc.oneapp.feature.curriculum.domain.usecase.GetCoursesUseCase
import com.xsc.oneapp.feature.curriculum.domain.usecase.GetProgrammesUseCase
import com.xsc.oneapp.feature.curriculum.domain.usecase.GetSyllabusUseCase
import com.xsc.sdk.network.APIError
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CurriculumViewModelTest {

    private lateinit var getProgrammesUseCase: GetProgrammesUseCase
    private lateinit var getCoursesUseCase: GetCoursesUseCase
    private lateinit var getSyllabusUseCase: GetSyllabusUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getProgrammesUseCase = mockk()
        getCoursesUseCase = mockk()
        getSyllabusUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): CurriculumViewModel = CurriculumViewModel(
        getProgrammesUseCase,
        getCoursesUseCase,
        getSyllabusUseCase
    )

    @Test
    fun `programmes load eagerly on init and surface a Success state`() = runTest {
        val programme = Programme("B.Tech CSE", "CSE", "8", "160")
        coEvery { getProgrammesUseCase() } returns listOf(programme)
        coEvery { getCoursesUseCase() } returns emptyList()
        coEvery { getSyllabusUseCase() } returns emptyList()

        val vm = viewModel()

        val state = vm.programmesState.value as UiState.Success
        assertEquals(listOf(programme), state.data)
    }

    @Test
    fun `courses only load once the Courses tab is selected`() = runTest {
        val course = Course("402", "CS101", "Introduction to Programming", "4.0", "3.0", "1.0", "0.0")
        coEvery { getProgrammesUseCase() } returns emptyList()
        coEvery { getCoursesUseCase() } returns listOf(course)
        coEvery { getSyllabusUseCase() } returns emptyList()

        val vm = viewModel()
        assertTrue(vm.coursesState.value is UiState.Loading)

        vm.onTabSelected(1)

        val state = vm.coursesState.value as UiState.Success
        assertEquals(listOf(course), state.data)
    }

    @Test
    fun `syllabus only loads once the Syllabus tab is selected`() = runTest {
        val entry = Syllabus("1", "CSE", "Semester 1 Curriculum")
        coEvery { getProgrammesUseCase() } returns emptyList()
        coEvery { getCoursesUseCase() } returns emptyList()
        coEvery { getSyllabusUseCase() } returns listOf(entry)

        val vm = viewModel()
        assertTrue(vm.syllabusState.value is UiState.Loading)

        vm.onTabSelected(2)

        val state = vm.syllabusState.value as UiState.Success
        assertEquals(listOf(entry), state.data)
    }

    @Test
    fun `no programmes is still a Success state with an empty list`() = runTest {
        coEvery { getProgrammesUseCase() } returns emptyList()
        coEvery { getCoursesUseCase() } returns emptyList()
        coEvery { getSyllabusUseCase() } returns emptyList()

        val vm = viewModel()

        val state = vm.programmesState.value as UiState.Success
        assertTrue(state.data.isEmpty())
    }

    @Test
    fun `an http error on programmes surfaces the status code`() = runTest {
        coEvery { getProgrammesUseCase() } throws APIError.HttpError(500, "boom")
        coEvery { getCoursesUseCase() } returns emptyList()
        coEvery { getSyllabusUseCase() } returns emptyList()

        val vm = viewModel()

        val state = vm.programmesState.value as UiState.UnexpectedError
        assertTrue(state.message.contains("500"))
    }

    @Test
    fun `a failing Courses section does not block Programmes`() = runTest {
        val programme = Programme("B.Tech CSE", "CSE", "8", "160")
        coEvery { getProgrammesUseCase() } returns listOf(programme)
        coEvery { getCoursesUseCase() } throws APIError.HttpError(500, "boom")
        coEvery { getSyllabusUseCase() } returns emptyList()

        val vm = viewModel()
        vm.onTabSelected(1)

        assertEquals(listOf(programme), (vm.programmesState.value as UiState.Success).data)
        assertTrue(vm.coursesState.value is UiState.UnexpectedError)
    }

    @Test
    fun `loadCourses retries only the courses section`() = runTest {
        coEvery { getProgrammesUseCase() } returns emptyList()
        coEvery { getSyllabusUseCase() } returns emptyList()
        coEvery { getCoursesUseCase() } throws APIError.NetworkError("offline") andThen listOf(
            Course("402", "CS101", "Introduction to Programming", "4.0", "3.0", "1.0", "0.0")
        )

        val vm = viewModel()
        vm.onTabSelected(1)
        assertTrue(vm.coursesState.value is UiState.NetworkError)

        vm.loadCourses()

        assertEquals(1, (vm.coursesState.value as UiState.Success).data.size)
    }
}
