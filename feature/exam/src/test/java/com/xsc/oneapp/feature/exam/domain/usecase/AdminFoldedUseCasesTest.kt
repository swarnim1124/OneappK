package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AdminFoldedUseCasesTest {

    @Test
    fun `ProcessRevaluationUseCase forwards revaluationRequestId and evaluatorId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.processRevaluation("1", "301") } just Runs

        ProcessRevaluationUseCase(repository)("1", "301")

        coVerify(exactly = 1) { repository.processRevaluation("1", "301") }
    }

    @Test
    fun `CreateSupplementaryExamUseCase forwards every field to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery {
            repository.createSupplementaryExam("Supplementary 2026", "2026-07-01", "2026-07-10", listOf("101", "102"))
        } just Runs

        CreateSupplementaryExamUseCase(repository)("Supplementary 2026", "2026-07-01", "2026-07-10", listOf("101", "102"))

        coVerify(exactly = 1) {
            repository.createSupplementaryExam("Supplementary 2026", "2026-07-01", "2026-07-10", listOf("101", "102"))
        }
    }

    @Test
    fun `CreateReappearExamUseCase forwards every field to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery {
            repository.createReappearExam(
                "Reappear 2026", "5", "2026-08-01", "2026-08-10", "2026-07-01", "2026-07-15", listOf("101")
            )
        } just Runs

        CreateReappearExamUseCase(repository)(
            "Reappear 2026", "5", "2026-08-01", "2026-08-10", "2026-07-01", "2026-07-15", listOf("101")
        )

        coVerify(exactly = 1) {
            repository.createReappearExam(
                "Reappear 2026", "5", "2026-08-01", "2026-08-10", "2026-07-01", "2026-07-15", listOf("101")
            )
        }
    }

    @Test
    fun `SetReappearEligibilityUseCase forwards every field to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.setReappearEligibility("45", "101", "1", true, "Cleared backlog") } just Runs

        SetReappearEligibilityUseCase(repository)("45", "101", "1", true, "Cleared backlog")

        coVerify(exactly = 1) { repository.setReappearEligibility("45", "101", "1", true, "Cleared backlog") }
    }

    @Test
    fun `ReportMalpracticeCaseUseCase forwards studentId, scheduleId and description`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.reportMalpracticeCase("45", "1", "Found with unauthorized notes") } just Runs

        ReportMalpracticeCaseUseCase(repository)("45", "1", "Found with unauthorized notes")

        coVerify(exactly = 1) { repository.reportMalpracticeCase("45", "1", "Found with unauthorized notes") }
    }

    @Test
    fun `RecordMalpracticeVerdictUseCase forwards every field to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery {
            repository.recordMalpracticeVerdict("1", "Disciplinary Committee", "2026-06-10", "WARNING")
        } just Runs

        RecordMalpracticeVerdictUseCase(repository)("1", "Disciplinary Committee", "2026-06-10", "WARNING")

        coVerify(exactly = 1) {
            repository.recordMalpracticeVerdict("1", "Disciplinary Committee", "2026-06-10", "WARNING")
        }
    }

    @Test
    fun `DismissMalpracticeCaseUseCase forwards caseId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.dismissMalpracticeCase("1") } just Runs

        DismissMalpracticeCaseUseCase(repository)("1")

        coVerify(exactly = 1) { repository.dismissMalpracticeCase("1") }
    }
}
