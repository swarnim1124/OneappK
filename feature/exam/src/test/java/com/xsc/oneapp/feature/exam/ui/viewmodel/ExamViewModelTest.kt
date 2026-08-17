package com.xsc.oneapp.feature.exam.ui.viewmodel

import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.exam.domain.model.ChallengeRevaluation
import com.xsc.oneapp.feature.exam.domain.model.ExamResult
import com.xsc.oneapp.feature.exam.domain.model.ExamSchedule
import com.xsc.oneapp.feature.exam.domain.model.RevaluationRequest
import com.xsc.oneapp.feature.exam.domain.usecase.GetChallengeRevaluationsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetExamResultsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetExamSchedulesUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetRevaluationRequestsUseCase
import com.xsc.sdk.network.APIError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExamViewModelTest {

    private lateinit var getExamSchedulesUseCase: GetExamSchedulesUseCase
    private lateinit var getExamResultsUseCase: GetExamResultsUseCase
    private lateinit var getRevaluationRequestsUseCase: GetRevaluationRequestsUseCase
    private lateinit var getChallengeRevaluationsUseCase: GetChallengeRevaluationsUseCase

    private val schedule = ExamSchedule("45", "Midterm", "2026-01-01", "2026-01-05", "published", "Written")
    private val result = ExamResult("1", "45", "1002", "8.4", "8.1", "PASS", "2026-01-10")

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getExamSchedulesUseCase = mockk()
        getExamResultsUseCase = mockk()
        getRevaluationRequestsUseCase = mockk()
        getChallengeRevaluationsUseCase = mockk()
        stubAllEmpty()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): ExamViewModel = ExamViewModel(
        getExamSchedulesUseCase,
        getExamResultsUseCase,
        getRevaluationRequestsUseCase,
        getChallengeRevaluationsUseCase
    )

    // --- Lazy loading -------------------------------------------------------

    @Test
    fun `constructing the ViewModel dispatches no requests at all`() = runTest {
        viewModel()

        coVerify(exactly = 0) { getExamSchedulesUseCase() }
        coVerify(exactly = 0) { getExamResultsUseCase() }
        coVerify(exactly = 0) { getRevaluationRequestsUseCase() }
        coVerify(exactly = 0) { getChallengeRevaluationsUseCase() }
    }

    @Test
    fun `loadOverview fetches only the three sections the overview renders`() = runTest {
        val vm = viewModel()

        vm.loadOverview()

        coVerify(exactly = 1) { getExamSchedulesUseCase() }
        coVerify(exactly = 1) { getExamResultsUseCase() }
        coVerify(exactly = 1) { getRevaluationRequestsUseCase() }
        coVerify(exactly = 0) { getChallengeRevaluationsUseCase() }
    }

    @Test
    fun `revisiting a section does not refetch it`() = runTest {
        val vm = viewModel()

        vm.loadSchedules()
        vm.loadSchedules()
        vm.loadSchedules()

        coVerify(exactly = 1) { getExamSchedulesUseCase() }
    }

    @Test
    fun `an explicit reload refetches even after a successful load`() = runTest {
        val vm = viewModel()

        vm.loadResults()
        vm.results.reload()

        coVerify(exactly = 2) { getExamResultsUseCase() }
    }

    @Test
    fun `sections shared across screens are fetched once, not once per screen`() = runTest {
        val vm = viewModel()

        // Schedules backs both the overview headline and the Schedules screen.
        vm.loadOverview()
        vm.loadSchedules()

        coVerify(exactly = 1) { getExamSchedulesUseCase() }
    }

    @Test
    fun `loadRevaluation fetches both requests and challenges together`() = runTest {
        val vm = viewModel()

        vm.loadRevaluation()

        coVerify(exactly = 1) { getRevaluationRequestsUseCase() }
        coVerify(exactly = 1) { getChallengeRevaluationsUseCase() }
    }

    // --- Section state ------------------------------------------------------

    @Test
    fun `schedules surface a Success state`() = runTest {
        coEvery { getExamSchedulesUseCase() } returns listOf(schedule)
        val vm = viewModel()

        vm.loadSchedules()

        assertEquals(listOf(schedule), (vm.schedules.state.value as UiState.Success).data)
    }

    @Test
    fun `an empty schedule list is still a Success state with an empty list`() = runTest {
        val vm = viewModel()

        vm.loadSchedules()

        assertTrue((vm.schedules.state.value as UiState.Success).data.isEmpty())
    }

    @Test
    fun `a business error surfaces its message directly`() = runTest {
        coEvery { getExamSchedulesUseCase() } throws APIError.BusinessError("NOT_FOUND", "No schedule published")
        val vm = viewModel()

        vm.loadSchedules()

        val state = vm.schedules.state.value as UiState.BusinessError
        assertEquals("No schedule published", state.message)
    }

    @Test
    fun `all four sections surface a Success state once loaded`() = runTest {
        val request = RevaluationRequest("1", "1002", "201", "45", "Marks seem low", "pending", "2026-01-11")
        val challenge = ChallengeRevaluation("1", "1002", "201", "45", "1", "Still disputed", "open", "2026-01-15")

        coEvery { getExamSchedulesUseCase() } returns listOf(schedule)
        coEvery { getExamResultsUseCase() } returns listOf(result)
        coEvery { getRevaluationRequestsUseCase() } returns listOf(request)
        coEvery { getChallengeRevaluationsUseCase() } returns listOf(challenge)

        val vm = viewModel()
        vm.loadOverview()
        vm.loadRevaluation()

        assertEquals(listOf(schedule), (vm.schedules.state.value as UiState.Success).data)
        assertEquals(listOf(result), (vm.results.state.value as UiState.Success).data)
        assertEquals(listOf(request), (vm.revaluationRequests.state.value as UiState.Success).data)
        assertEquals(listOf(challenge), (vm.challengeRevaluations.state.value as UiState.Success).data)
    }

    // --- Derived overview ---------------------------------------------------

    @Test
    fun `overview surfaces schedule count, next schedule and latest result`() = runTest {
        val olderResult = ExamResult("0", "44", "1002", "7.0", "7.2", "PASS", "2025-06-01")
        coEvery { getExamSchedulesUseCase() } returns listOf(schedule)
        coEvery { getExamResultsUseCase() } returns listOf(olderResult, result)
        coEvery { getRevaluationRequestsUseCase() } returns listOf(
            RevaluationRequest("1", "1002", "201", "45", "Marks seem low", "pending", "2026-01-11")
        )

        val vm = viewModel()
        vm.loadOverview()

        val overview = vm.overview.value
        assertEquals(1, overview.scheduleCount)
        assertEquals(schedule, overview.nextSchedule)
        assertEquals(result, overview.latestResult)
        assertEquals(1, overview.revaluationCount)
        assertNull(overview.errorMessage)
    }

    @Test
    fun `a failing secondary section degrades its part of the overview instead of erroring the screen`() = runTest {
        coEvery { getExamSchedulesUseCase() } returns listOf(schedule)
        coEvery { getExamResultsUseCase() } throws APIError.NetworkError("Unable to reach the server")

        val vm = viewModel()
        vm.loadOverview()

        val overview = vm.overview.value
        // The headline still has a schedule to fall back on; only the result is absent.
        assertEquals(schedule, overview.nextSchedule)
        assertNull(overview.latestResult)
        assertNull(overview.errorMessage)
    }

    @Test
    fun `a failing schedules section does surface an error for the whole overview`() = runTest {
        coEvery { getExamSchedulesUseCase() } throws APIError.NetworkError("Unable to reach the server")

        val vm = viewModel()
        vm.loadOverview()

        assertEquals("Unable to reach the server", vm.overview.value.errorMessage)
    }

    private fun stubAllEmpty() {
        coEvery { getExamSchedulesUseCase() } returns emptyList()
        coEvery { getExamResultsUseCase() } returns emptyList()
        coEvery { getRevaluationRequestsUseCase() } returns emptyList()
        coEvery { getChallengeRevaluationsUseCase() } returns emptyList()
    }
}
