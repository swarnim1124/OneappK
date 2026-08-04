package com.xsc.oneapp.feature.exam.ui.viewmodel

import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.exam.domain.model.ExamSchedule
import com.xsc.oneapp.feature.exam.domain.usecase.GetExamSchedulesUseCase
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
class ExamViewModelTest {

    private lateinit var getExamSchedulesUseCase: GetExamSchedulesUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getExamSchedulesUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `schedules from the use case surface a Success state`() = runTest {
        val schedule = ExamSchedule("45", "Midterm", "2026-01-01", "2026-01-05", "Published", "Written")
        coEvery { getExamSchedulesUseCase() } returns listOf(schedule)

        val vm = ExamViewModel(getExamSchedulesUseCase)

        val state = vm.state.value as UiState.Success
        assertEquals(listOf(schedule), state.data)
    }

    @Test
    fun `an empty schedule list is still a Success state with an empty list`() = runTest {
        coEvery { getExamSchedulesUseCase() } returns emptyList()

        val vm = ExamViewModel(getExamSchedulesUseCase)

        val state = vm.state.value as UiState.Success
        assertTrue(state.data.isEmpty())
    }

    @Test
    fun `a business error surfaces its message directly`() = runTest {
        coEvery { getExamSchedulesUseCase() } throws APIError.BusinessError("NOT_FOUND", "No schedule published")

        val vm = ExamViewModel(getExamSchedulesUseCase)

        val state = vm.state.value as UiState.BusinessError
        assertEquals("No schedule published", state.message)
    }

    @Test
    fun `a network error surfaces as a NetworkError state`() = runTest {
        coEvery { getExamSchedulesUseCase() } throws APIError.NetworkError("timeout")

        val vm = ExamViewModel(getExamSchedulesUseCase)

        val state = vm.state.value as UiState.NetworkError
        assertEquals("timeout", state.message)
    }
}
