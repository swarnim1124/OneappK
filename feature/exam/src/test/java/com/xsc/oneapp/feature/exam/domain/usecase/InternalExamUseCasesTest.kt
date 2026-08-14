package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.InternalExam
import com.xsc.oneapp.feature.exam.domain.model.MarksRecord
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class InternalExamUseCasesTest {

    @Test
    fun `GetInternalExamsUseCase returns exams for the given course offering`() = runTest {
        val repository = mockk<ExamRepository>()
        val exams = listOf(InternalExam("1", "201", "CIE-1", "5", "2026-06-01", "20", "SCHEDULED"))
        coEvery { repository.getInternalExams("201") } returns exams

        val result = GetInternalExamsUseCase(repository)("201")

        assertEquals(exams, result)
    }

    @Test
    fun `AddInternalExamUseCase forwards every field to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.addInternalExam("201", "CIE-1", "5", "2026-06-01", "20") } just Runs

        AddInternalExamUseCase(repository)("201", "CIE-1", "5", "2026-06-01", "20")

        coVerify(exactly = 1) { repository.addInternalExam("201", "CIE-1", "5", "2026-06-01", "20") }
    }

    @Test
    fun `UpdateInternalExamUseCase forwards id and updated fields`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.updateInternalExam("1", "CIE-1 Retest", "2026-06-05", "25", "SCHEDULED") } just Runs

        UpdateInternalExamUseCase(repository)("1", "CIE-1 Retest", "2026-06-05", "25", "SCHEDULED")

        coVerify(exactly = 1) {
            repository.updateInternalExam("1", "CIE-1 Retest", "2026-06-05", "25", "SCHEDULED")
        }
    }

    @Test
    fun `DeleteInternalExamUseCase forwards id`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.deleteInternalExam("1") } just Runs

        DeleteInternalExamUseCase(repository)("1")

        coVerify(exactly = 1) { repository.deleteInternalExam("1") }
    }

    @Test
    fun `GetInternalMarksEntriesUseCase returns entries for the given internal exam`() = runTest {
        val repository = mockk<ExamRepository>()
        val records = listOf(MarksRecord("1", null, null, "101", "18", "SUBMITTED"))
        coEvery { repository.getInternalMarksEntries("1") } returns records

        val result = GetInternalMarksEntriesUseCase(repository)("1")

        assertEquals(records, result)
    }

    @Test
    fun `SubmitInternalMarksEntryUseCase forwards internalExamId and records`() = runTest {
        val repository = mockk<ExamRepository>()
        val records = listOf("101" to "85", "102" to "90")
        coEvery { repository.submitInternalMarksEntry("1", records) } just Runs

        SubmitInternalMarksEntryUseCase(repository)("1", records)

        coVerify(exactly = 1) { repository.submitInternalMarksEntry("1", records) }
    }

    @Test
    fun `ConsolidateInternalMarksUseCase forwards courseOfferingId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.consolidateInternalMarks("201") } just Runs

        ConsolidateInternalMarksUseCase(repository)("201")

        coVerify(exactly = 1) { repository.consolidateInternalMarks("201") }
    }
}
