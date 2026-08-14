package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.ExternalEvaluation
import com.xsc.oneapp.feature.exam.domain.model.ExternalExaminer
import com.xsc.oneapp.feature.exam.domain.model.ExternalPaperSetting
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ExternalExamUseCasesTest {

    @Test
    fun `GetExternalExaminersUseCase returns examiners from the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        val examiners = listOf(ExternalExaminer("1", "Dr. Rao", "rao@example.com", "9876543210", "Physics"))
        coEvery { repository.getExternalExaminers() } returns examiners

        val result = GetExternalExaminersUseCase(repository)()

        assertEquals(examiners, result)
    }

    @Test
    fun `AddExternalExaminerUseCase forwards every field to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery {
            repository.addExternalExaminer("Dr. Rao", "rao@example.com", "9876543210", "Physics")
        } just Runs

        AddExternalExaminerUseCase(repository)("Dr. Rao", "rao@example.com", "9876543210", "Physics")

        coVerify(exactly = 1) {
            repository.addExternalExaminer("Dr. Rao", "rao@example.com", "9876543210", "Physics")
        }
    }

    @Test
    fun `UpdateExternalExaminerUseCase forwards id and updated fields`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery {
            repository.updateExternalExaminer("1", "Dr. S. Rao", "s.rao@example.com", "9876500000", "Chemistry")
        } just Runs

        UpdateExternalExaminerUseCase(repository)("1", "Dr. S. Rao", "s.rao@example.com", "9876500000", "Chemistry")

        coVerify(exactly = 1) {
            repository.updateExternalExaminer("1", "Dr. S. Rao", "s.rao@example.com", "9876500000", "Chemistry")
        }
    }

    @Test
    fun `DeleteExternalExaminerUseCase forwards id`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.deleteExternalExaminer("1") } just Runs

        DeleteExternalExaminerUseCase(repository)("1")

        coVerify(exactly = 1) { repository.deleteExternalExaminer("1") }
    }

    @Test
    fun `GetExternalPaperSettingsUseCase returns settings from the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        val settings = listOf(ExternalPaperSetting("1", "1", "101", "1"))
        coEvery { repository.getExternalPaperSettings() } returns settings

        val result = GetExternalPaperSettingsUseCase(repository)()

        assertEquals(settings, result)
    }

    @Test
    fun `AddExternalPaperSettingUseCase forwards every field to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.addExternalPaperSetting("1", "101", "1") } just Runs

        AddExternalPaperSettingUseCase(repository)("1", "101", "1")

        coVerify(exactly = 1) { repository.addExternalPaperSetting("1", "101", "1") }
    }

    @Test
    fun `DeleteExternalPaperSettingUseCase forwards id`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.deleteExternalPaperSetting("1") } just Runs

        DeleteExternalPaperSettingUseCase(repository)("1")

        coVerify(exactly = 1) { repository.deleteExternalPaperSetting("1") }
    }

    @Test
    fun `GetExternalEvaluationsUseCase returns evaluations from the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        val evaluations = listOf(ExternalEvaluation("1", "1", "101", "1", listOf("B-1", "B-2"), "DISPATCHED"))
        coEvery { repository.getExternalEvaluations() } returns evaluations

        val result = GetExternalEvaluationsUseCase(repository)()

        assertEquals(evaluations, result)
    }

    @Test
    fun `AddExternalEvaluationUseCase forwards every field to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.addExternalEvaluation("1", "101", "1", listOf("B-1", "B-2")) } just Runs

        AddExternalEvaluationUseCase(repository)("1", "101", "1", listOf("B-1", "B-2"))

        coVerify(exactly = 1) { repository.addExternalEvaluation("1", "101", "1", listOf("B-1", "B-2")) }
    }

    @Test
    fun `UpdateExternalEvaluationUseCase forwards id and status`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.updateExternalEvaluation("1", "COMPLETED") } just Runs

        UpdateExternalEvaluationUseCase(repository)("1", "COMPLETED")

        coVerify(exactly = 1) { repository.updateExternalEvaluation("1", "COMPLETED") }
    }

    @Test
    fun `DeleteExternalEvaluationUseCase forwards id`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.deleteExternalEvaluation("1") } just Runs

        DeleteExternalEvaluationUseCase(repository)("1")

        coVerify(exactly = 1) { repository.deleteExternalEvaluation("1") }
    }
}
