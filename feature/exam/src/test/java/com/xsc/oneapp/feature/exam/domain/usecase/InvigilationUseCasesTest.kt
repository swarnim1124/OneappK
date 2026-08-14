package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.InvigilatorAssignment
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class InvigilationUseCasesTest {

    @Test
    fun `GetInvigilatorAssignmentsUseCase returns assignments for the given schedule`() = runTest {
        val repository = mockk<ExamRepository>()
        val assignments = listOf(InvigilatorAssignment("1", "1", "10", "201", "CHIEF", "ASSIGNED"))
        coEvery { repository.getInvigilatorAssignments("1") } returns assignments

        val result = GetInvigilatorAssignmentsUseCase(repository)("1")

        assertEquals(assignments, result)
    }

    @Test
    fun `AddInvigilatorAssignmentUseCase forwards every field to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.addInvigilatorAssignment("1", "10", "201", "CHIEF") } just Runs

        AddInvigilatorAssignmentUseCase(repository)("1", "10", "201", "CHIEF")

        coVerify(exactly = 1) { repository.addInvigilatorAssignment("1", "10", "201", "CHIEF") }
    }

    @Test
    fun `UpdateInvigilatorAssignmentUseCase forwards id and updated fields`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.updateInvigilatorAssignment("1", "ASSISTANT", "CONFIRMED") } just Runs

        UpdateInvigilatorAssignmentUseCase(repository)("1", "ASSISTANT", "CONFIRMED")

        coVerify(exactly = 1) { repository.updateInvigilatorAssignment("1", "ASSISTANT", "CONFIRMED") }
    }

    @Test
    fun `DeleteInvigilatorAssignmentUseCase forwards id`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.deleteInvigilatorAssignment("1") } just Runs

        DeleteInvigilatorAssignmentUseCase(repository)("1")

        coVerify(exactly = 1) { repository.deleteInvigilatorAssignment("1") }
    }
}
