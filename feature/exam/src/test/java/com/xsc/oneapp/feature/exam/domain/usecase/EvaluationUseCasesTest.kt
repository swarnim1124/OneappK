package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.EvaluationBundle
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class EvaluationUseCasesTest {

    @Test
    fun `GetEvaluationBundlesUseCase returns bundles for the given schedule`() = runTest {
        val repository = mockk<ExamRepository>()
        val bundles = listOf(EvaluationBundle("1", "1", "101", "301", listOf("45", "46"), "DISPATCHED"))
        coEvery { repository.getEvaluationBundles("1") } returns bundles

        val result = GetEvaluationBundlesUseCase(repository)("1")

        assertEquals(bundles, result)
    }

    @Test
    fun `AddEvaluationBundleUseCase forwards every field to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.addEvaluationBundle("1", "101", "301", listOf("45", "46")) } just Runs

        AddEvaluationBundleUseCase(repository)("1", "101", "301", listOf("45", "46"))

        coVerify(exactly = 1) { repository.addEvaluationBundle("1", "101", "301", listOf("45", "46")) }
    }

    @Test
    fun `UpdateEvaluationBundleUseCase forwards id and status`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.updateEvaluationBundle("1", "COMPLETED") } just Runs

        UpdateEvaluationBundleUseCase(repository)("1", "COMPLETED")

        coVerify(exactly = 1) { repository.updateEvaluationBundle("1", "COMPLETED") }
    }

    @Test
    fun `DeleteEvaluationBundleUseCase forwards id`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.deleteEvaluationBundle("1") } just Runs

        DeleteEvaluationBundleUseCase(repository)("1")

        coVerify(exactly = 1) { repository.deleteEvaluationBundle("1") }
    }

    @Test
    fun `RequestSecondValuationUseCase forwards scheduleId, courseId and studentId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.requestSecondValuation("1", "101", "45") } just Runs

        RequestSecondValuationUseCase(repository)("1", "101", "45")

        coVerify(exactly = 1) { repository.requestSecondValuation("1", "101", "45") }
    }

    @Test
    fun `ApplyModerationUseCase forwards scheduleId, courseId and adjustment`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.applyModeration("1", "101", "5") } just Runs

        ApplyModerationUseCase(repository)("1", "101", "5")

        coVerify(exactly = 1) { repository.applyModeration("1", "101", "5") }
    }
}
