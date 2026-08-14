package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.Grade
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

class MarksGradingUseCasesTest {

    @Test
    fun `GetMarksEntriesUseCase returns entries for the given schedule and course`() = runTest {
        val repository = mockk<ExamRepository>()
        val records = listOf(MarksRecord("1", "1", "101", "45", "85", "SUBMITTED"))
        coEvery { repository.getMarksEntries("1", "101") } returns records

        val result = GetMarksEntriesUseCase(repository)("1", "101")

        assertEquals(records, result)
    }

    @Test
    fun `SubmitMarksEntryUseCase forwards scheduleId, courseId and records`() = runTest {
        val repository = mockk<ExamRepository>()
        val records = listOf("101" to "85", "102" to "90")
        coEvery { repository.submitMarksEntry("1", "101", records) } just Runs

        SubmitMarksEntryUseCase(repository)("1", "101", records)

        coVerify(exactly = 1) { repository.submitMarksEntry("1", "101", records) }
    }

    @Test
    fun `VerifyMarksUseCase forwards scheduleId and courseId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.verifyMarks("1", "101") } just Runs

        VerifyMarksUseCase(repository)("1", "101")

        coVerify(exactly = 1) { repository.verifyMarks("1", "101") }
    }

    @Test
    fun `GetGradesUseCase returns grades for the given schedule`() = runTest {
        val repository = mockk<ExamRepository>()
        val grades = listOf(Grade("1", "1", "45", "101", "A", "GENERATED"))
        coEvery { repository.getGrades("1") } returns grades

        val result = GetGradesUseCase(repository)("1")

        assertEquals(grades, result)
    }

    @Test
    fun `GenerateGradesUseCase forwards scheduleId and courseId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.generateGrades("1", "101") } just Runs

        GenerateGradesUseCase(repository)("1", "101")

        coVerify(exactly = 1) { repository.generateGrades("1", "101") }
    }

    @Test
    fun `LockGradesUseCase forwards scheduleId and courseId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.lockGrades("1", "101") } just Runs

        LockGradesUseCase(repository)("1", "101")

        coVerify(exactly = 1) { repository.lockGrades("1", "101") }
    }
}
