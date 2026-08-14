package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.ReappearEligibility
import com.xsc.oneapp.feature.exam.domain.model.ReappearExam
import com.xsc.oneapp.feature.exam.domain.model.SupplementaryExam
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ReExamUseCasesTest {

    @Test
    fun `GetSupplementaryExamsUseCase delegates to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        val exams = listOf(SupplementaryExam("7", "Backlog Nov 2026", "2026-11-01", "2026-11-05", "scheduled", listOf("101")))
        coEvery { repository.getSupplementaryExams() } returns exams

        val result = GetSupplementaryExamsUseCase(repository).invoke()

        assertEquals(exams, result)
    }

    @Test
    fun `RegisterForSupplementaryExamUseCase forwards the exam id and course ids to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.registerForSupplementaryExam("7", listOf("101", "102")) } just Runs

        RegisterForSupplementaryExamUseCase(repository).invoke("7", listOf("101", "102"))

        coVerify(exactly = 1) { repository.registerForSupplementaryExam("7", listOf("101", "102")) }
    }

    @Test
    fun `GetReappearExamsUseCase delegates to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        val exams = listOf(
            ReappearExam("3", "Improvement Dec 2026", "T1", "2026-12-01", "2026-12-05", "2026-11-01", "2026-11-15", "scheduled", listOf("201"))
        )
        coEvery { repository.getReappearExams() } returns exams

        val result = GetReappearExamsUseCase(repository).invoke()

        assertEquals(exams, result)
    }

    @Test
    fun `RegisterForReappearExamUseCase forwards the exam id and course ids to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.registerForReappearExam("3", listOf("201")) } just Runs

        RegisterForReappearExamUseCase(repository).invoke("3", listOf("201"))

        coVerify(exactly = 1) { repository.registerForReappearExam("3", listOf("201")) }
    }

    @Test
    fun `GetMyReappearEligibilityUseCase delegates the optional reappearExamId to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        val eligibility = listOf(ReappearEligibility("1", "101", "5", "3", true, null, "active"))
        coEvery { repository.getMyReappearEligibility("3") } returns eligibility

        val result = GetMyReappearEligibilityUseCase(repository).invoke("3")

        assertEquals(eligibility, result)
    }
}
