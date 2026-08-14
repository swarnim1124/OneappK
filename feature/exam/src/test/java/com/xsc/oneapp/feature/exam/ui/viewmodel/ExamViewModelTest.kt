package com.xsc.oneapp.feature.exam.ui.viewmodel

import app.cash.turbine.test
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.exam.domain.model.ChallengeRevaluation
import com.xsc.oneapp.feature.exam.domain.model.ExamAppeal
import com.xsc.oneapp.feature.exam.domain.model.ExamResult
import com.xsc.oneapp.feature.exam.domain.model.ExamSchedule
import com.xsc.oneapp.feature.exam.domain.model.MalpracticeCase
import com.xsc.oneapp.feature.exam.domain.model.PhotocopyRequest
import com.xsc.oneapp.feature.exam.domain.model.ReappearExam
import com.xsc.oneapp.feature.exam.domain.model.RevaluationRequest
import com.xsc.oneapp.feature.exam.domain.model.SupplementaryExam
import com.xsc.oneapp.feature.exam.domain.usecase.GetChallengeRevaluationsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetExamResultsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetExamSchedulesUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetMyAppealsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetMyMalpracticeCasesUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetMyPhotocopyRequestsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetReappearExamsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetRevaluationRequestsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetSupplementaryExamsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.RegisterForReappearExamUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.RegisterForSupplementaryExamUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.SubmitAppealUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.SubmitChallengeRevaluationUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.SubmitPhotocopyRequestUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.SubmitRevaluationRequestUseCase
import com.xsc.oneapp.feature.exam.ui.state.ExamEffect
import com.xsc.sdk.network.APIError
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
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
    private lateinit var getExamResultsUseCase: GetExamResultsUseCase
    private lateinit var getRevaluationRequestsUseCase: GetRevaluationRequestsUseCase
    private lateinit var getMyPhotocopyRequestsUseCase: GetMyPhotocopyRequestsUseCase
    private lateinit var getMyAppealsUseCase: GetMyAppealsUseCase
    private lateinit var getChallengeRevaluationsUseCase: GetChallengeRevaluationsUseCase
    private lateinit var getSupplementaryExamsUseCase: GetSupplementaryExamsUseCase
    private lateinit var getReappearExamsUseCase: GetReappearExamsUseCase
    private lateinit var getMyMalpracticeCasesUseCase: GetMyMalpracticeCasesUseCase
    private lateinit var submitRevaluationRequestUseCase: SubmitRevaluationRequestUseCase
    private lateinit var submitPhotocopyRequestUseCase: SubmitPhotocopyRequestUseCase
    private lateinit var submitAppealUseCase: SubmitAppealUseCase
    private lateinit var submitChallengeRevaluationUseCase: SubmitChallengeRevaluationUseCase
    private lateinit var registerForSupplementaryExamUseCase: RegisterForSupplementaryExamUseCase
    private lateinit var registerForReappearExamUseCase: RegisterForReappearExamUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getExamSchedulesUseCase = mockk()
        getExamResultsUseCase = mockk()
        getRevaluationRequestsUseCase = mockk()
        getMyPhotocopyRequestsUseCase = mockk()
        getMyAppealsUseCase = mockk()
        getChallengeRevaluationsUseCase = mockk()
        getSupplementaryExamsUseCase = mockk()
        getReappearExamsUseCase = mockk()
        getMyMalpracticeCasesUseCase = mockk()
        submitRevaluationRequestUseCase = mockk()
        submitPhotocopyRequestUseCase = mockk()
        submitAppealUseCase = mockk()
        submitChallengeRevaluationUseCase = mockk()
        registerForSupplementaryExamUseCase = mockk()
        registerForReappearExamUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = ExamViewModel(
        getExamSchedulesUseCase,
        getExamResultsUseCase,
        getRevaluationRequestsUseCase,
        getMyPhotocopyRequestsUseCase,
        getMyAppealsUseCase,
        getChallengeRevaluationsUseCase,
        getSupplementaryExamsUseCase,
        getReappearExamsUseCase,
        getMyMalpracticeCasesUseCase,
        submitRevaluationRequestUseCase,
        submitPhotocopyRequestUseCase,
        submitAppealUseCase,
        submitChallengeRevaluationUseCase,
        registerForSupplementaryExamUseCase,
        registerForReappearExamUseCase
    )

    @Test
    fun `schedules from the use case surface a Success state`() = runTest {
        val schedule = ExamSchedule("45", "Midterm", "2026-01-01", "2026-01-05", "Published", "Written")
        coEvery { getExamSchedulesUseCase() } returns listOf(schedule)

        val vm = viewModel()
        vm.loadSchedules()

        val state = vm.schedules.state.value as UiState.Success
        assertEquals(listOf(schedule), state.data)
    }

    @Test
    fun `an empty schedule list is still a Success state with an empty list`() = runTest {
        coEvery { getExamSchedulesUseCase() } returns emptyList()

        val vm = viewModel()
        vm.loadSchedules()

        val state = vm.schedules.state.value as UiState.Success
        assertTrue(state.data.isEmpty())
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
    fun `a network error surfaces as a NetworkError state`() = runTest {
        // uiStateCatching maps this to a fixed, reason-based message now (see
        // core.result.AppError.Network) rather than passing "timeout" through -
        // NetworkError("timeout") with no reason defaults to UNREACHABLE.
        coEvery { getExamSchedulesUseCase() } throws APIError.NetworkError("timeout")

        val vm = viewModel()
        vm.loadSchedules()

        val state = vm.schedules.state.value as UiState.NetworkError
        assertEquals("Server is unreachable. Try again in a few minutes.", state.message)
    }

    @Test
    fun `results load independently of schedules`() = runTest {
        val result = ExamResult("1", "45", "1002", "8.5", "8.2", "pass", "2026-08-01")
        coEvery { getExamResultsUseCase() } returns listOf(result)

        val vm = viewModel()
        vm.loadResults()

        val state = vm.results.state.value as UiState.Success
        assertEquals(listOf(result), state.data)
    }

    @Test
    fun `loadRevaluation loads all four revaluation sections`() = runTest {
        coEvery { getRevaluationRequestsUseCase() } returns emptyList()
        coEvery { getMyPhotocopyRequestsUseCase() } returns emptyList()
        coEvery { getMyAppealsUseCase() } returns emptyList()
        coEvery { getChallengeRevaluationsUseCase() } returns emptyList()

        val vm = viewModel()
        vm.loadRevaluation()

        assertTrue(vm.revaluationRequests.state.value is UiState.Success)
        assertTrue(vm.photocopyRequests.state.value is UiState.Success)
        assertTrue(vm.appeals.state.value is UiState.Success)
        assertTrue(vm.challengeRevaluations.state.value is UiState.Success)
    }

    @Test
    fun `submitting a revaluation request reloads the requests section and emits a toast`() = runTest {
        coEvery { getRevaluationRequestsUseCase() } returns emptyList()
        coEvery { submitRevaluationRequestUseCase("45", "101", "Marks seem wrong") } just Runs

        val vm = viewModel()
        vm.loadRevaluation()

        vm.effect.test {
            vm.submitRevaluationRequest("45", "101", "Marks seem wrong")
            assertEquals(ExamEffect.ShowToast("Revaluation request submitted"), awaitItem())
        }
        coVerify(exactly = 1) { submitRevaluationRequestUseCase("45", "101", "Marks seem wrong") }
        coVerify(exactly = 2) { getRevaluationRequestsUseCase() } // initial load + post-submit reload
    }

    @Test
    fun `a failed submit emits the business error message instead of a generic one`() = runTest {
        coEvery { getMyPhotocopyRequestsUseCase() } returns emptyList()
        coEvery { submitPhotocopyRequestUseCase("45", "101") } throws
            APIError.BusinessError("DUPLICATE", "A photocopy request already exists for this course")

        val vm = viewModel()
        vm.loadRevaluation()

        vm.effect.test {
            vm.submitPhotocopyRequest("45", "101")
            assertEquals(
                ExamEffect.ShowToast("A photocopy request already exists for this course"),
                awaitItem()
            )
        }
    }

    @Test
    fun `submitting an appeal reloads appeals`() = runTest {
        coEvery { getMyAppealsUseCase() } returns emptyList()
        coEvery { submitAppealUseCase("9", "Outcome was unfair") } just Runs

        val vm = viewModel()
        vm.loadRevaluation()
        vm.submitAppeal("9", "Outcome was unfair")

        coVerify(exactly = 1) { submitAppealUseCase("9", "Outcome was unfair") }
        coVerify(exactly = 2) { getMyAppealsUseCase() }
    }

    @Test
    fun `submitting a challenge revaluation reloads challenge revaluations`() = runTest {
        coEvery { getChallengeRevaluationsUseCase() } returns emptyList()
        coEvery { submitChallengeRevaluationUseCase("45", "101", "9") } just Runs

        val vm = viewModel()
        vm.loadRevaluation()
        vm.submitChallengeRevaluation("45", "101", "9")

        coVerify(exactly = 1) { submitChallengeRevaluationUseCase("45", "101", "9") }
        coVerify(exactly = 2) { getChallengeRevaluationsUseCase() }
    }

    @Test
    fun `loadReExams loads both supplementary and reappear sections`() = runTest {
        coEvery { getSupplementaryExamsUseCase() } returns emptyList()
        coEvery { getReappearExamsUseCase() } returns emptyList()

        val vm = viewModel()
        vm.loadReExams()

        assertTrue(vm.supplementaryExams.state.value is UiState.Success)
        assertTrue(vm.reappearExams.state.value is UiState.Success)
    }

    @Test
    fun `registering for a supplementary exam reloads the supplementary section`() = runTest {
        coEvery { getSupplementaryExamsUseCase() } returns emptyList()
        coEvery { registerForSupplementaryExamUseCase("7", listOf("101", "102")) } just Runs

        val vm = viewModel()
        vm.loadReExams()
        vm.registerForSupplementaryExam("7", listOf("101", "102"))

        coVerify(exactly = 1) { registerForSupplementaryExamUseCase("7", listOf("101", "102")) }
        coVerify(exactly = 2) { getSupplementaryExamsUseCase() }
    }

    @Test
    fun `registering for a reappear exam reloads the reappear section`() = runTest {
        coEvery { getReappearExamsUseCase() } returns emptyList()
        coEvery { registerForReappearExamUseCase("3", listOf("201")) } just Runs

        val vm = viewModel()
        vm.loadReExams()
        vm.registerForReappearExam("3", listOf("201"))

        coVerify(exactly = 1) { registerForReappearExamUseCase("3", listOf("201")) }
        coVerify(exactly = 2) { getReappearExamsUseCase() }
    }

    @Test
    fun `malpractice cases load into their own section`() = runTest {
        val case = MalpracticeCase(
            "1", "1002", "45", "Unauthorized material", "Invigilator A",
            null, null, null, "pending", "2026-08-01"
        )
        coEvery { getMyMalpracticeCasesUseCase() } returns listOf(case)

        val vm = viewModel()
        vm.loadMalpractice()

        val state = vm.malpracticeCases.state.value as UiState.Success
        assertEquals(listOf(case), state.data)
    }
}
