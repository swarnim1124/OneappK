package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.StudentExamBlock
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class HallTicketAdminUseCasesTest {

    @Test
    fun `GenerateHallTicketUseCase forwards every field to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.generateHallTicket("1", "45", "Main Hall", "A1") } just Runs

        GenerateHallTicketUseCase(repository)("1", "45", "Main Hall", "A1")

        coVerify(exactly = 1) { repository.generateHallTicket("1", "45", "Main Hall", "A1") }
    }

    @Test
    fun `UpdateHallTicketAdminUseCase forwards id and updated fields`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.updateHallTicketAdmin("1", "North Hall", "B2") } just Runs

        UpdateHallTicketAdminUseCase(repository)("1", "North Hall", "B2")

        coVerify(exactly = 1) { repository.updateHallTicketAdmin("1", "North Hall", "B2") }
    }

    @Test
    fun `DeleteHallTicketAdminUseCase forwards id`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.deleteHallTicketAdmin("1") } just Runs

        DeleteHallTicketAdminUseCase(repository)("1")

        coVerify(exactly = 1) { repository.deleteHallTicketAdmin("1") }
    }

    @Test
    fun `PublishHallTicketsUseCase forwards scheduleId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.publishHallTickets("1") } just Runs

        PublishHallTicketsUseCase(repository)("1")

        coVerify(exactly = 1) { repository.publishHallTickets("1") }
    }

    @Test
    fun `HoldHallTicketsUseCase forwards scheduleId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.holdHallTickets("1") } just Runs

        HoldHallTicketsUseCase(repository)("1")

        coVerify(exactly = 1) { repository.holdHallTickets("1") }
    }

    @Test
    fun `UnpublishHallTicketsUseCase forwards scheduleId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.unpublishHallTickets("1") } just Runs

        UnpublishHallTicketsUseCase(repository)("1")

        coVerify(exactly = 1) { repository.unpublishHallTickets("1") }
    }

    @Test
    fun `GetExamBlocksAdminUseCase returns blocks for the given schedule`() = runTest {
        val repository = mockk<ExamRepository>()
        val blocks = listOf(StudentExamBlock("1", "45", "1", "Fee arrears", "ADMIN", "ACTIVE", "2026-06-01"))
        coEvery { repository.getExamBlocksAdmin("1") } returns blocks

        val result = GetExamBlocksAdminUseCase(repository)("1")

        assertEquals(blocks, result)
    }

    @Test
    fun `AddStudentExamBlockUseCase forwards every field to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.addStudentExamBlock("45", "1", "Fee arrears") } just Runs

        AddStudentExamBlockUseCase(repository)("45", "1", "Fee arrears")

        coVerify(exactly = 1) { repository.addStudentExamBlock("45", "1", "Fee arrears") }
    }

    @Test
    fun `UpdateStudentExamBlockUseCase forwards id and updated fields`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.updateStudentExamBlock("1", "Cleared", "RESOLVED") } just Runs

        UpdateStudentExamBlockUseCase(repository)("1", "Cleared", "RESOLVED")

        coVerify(exactly = 1) { repository.updateStudentExamBlock("1", "Cleared", "RESOLVED") }
    }

    @Test
    fun `DeleteStudentExamBlockUseCase forwards id`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.deleteStudentExamBlock("1") } just Runs

        DeleteStudentExamBlockUseCase(repository)("1")

        coVerify(exactly = 1) { repository.deleteStudentExamBlock("1") }
    }
}
