package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.ExamAppeal
import com.xsc.oneapp.feature.exam.domain.model.PhotocopyRequest
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RevaluationUseCasesTest {

    @Test
    fun `SubmitRevaluationRequestUseCase forwards scheduleId, courseId and reason to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.submitRevaluationRequest("10", "5", "Marks seem wrong") } just Runs

        SubmitRevaluationRequestUseCase(repository).invoke("10", "5", "Marks seem wrong")

        coVerify(exactly = 1) { repository.submitRevaluationRequest("10", "5", "Marks seem wrong") }
    }

    @Test
    fun `GetMyPhotocopyRequestsUseCase delegates to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        val requests = listOf(PhotocopyRequest("1", "101", "10", "5", "submitted", "2026-08-01"))
        coEvery { repository.getMyPhotocopyRequests() } returns requests

        val result = GetMyPhotocopyRequestsUseCase(repository).invoke()

        assertEquals(requests, result)
    }

    @Test
    fun `SubmitPhotocopyRequestUseCase forwards scheduleId and courseId to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.submitPhotocopyRequest("10", "5") } just Runs

        SubmitPhotocopyRequestUseCase(repository).invoke("10", "5")

        coVerify(exactly = 1) { repository.submitPhotocopyRequest("10", "5") }
    }

    @Test
    fun `GetMyAppealsUseCase delegates to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        val appeals = listOf(ExamAppeal("1", "101", "9", "Unfair outcome", "submitted", "2026-08-01"))
        coEvery { repository.getMyAppeals() } returns appeals

        val result = GetMyAppealsUseCase(repository).invoke()

        assertEquals(appeals, result)
    }

    @Test
    fun `SubmitAppealUseCase forwards referenceId and reason to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.submitAppeal("9", "Unfair outcome") } just Runs

        SubmitAppealUseCase(repository).invoke("9", "Unfair outcome")

        coVerify(exactly = 1) { repository.submitAppeal("9", "Unfair outcome") }
    }

    @Test
    fun `SubmitChallengeRevaluationUseCase forwards scheduleId, courseId and the prior revaluation request id`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.submitChallengeRevaluation("10", "5", "9") } just Runs

        SubmitChallengeRevaluationUseCase(repository).invoke("10", "5", "9")

        coVerify(exactly = 1) { repository.submitChallengeRevaluation("10", "5", "9") }
    }
}
