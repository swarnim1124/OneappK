package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.ResultApprovalStatus
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ResultAdminUseCasesTest {

    @Test
    fun `GenerateResultsUseCase forwards scheduleId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.generateResults("1") } just Runs

        GenerateResultsUseCase(repository)("1")

        coVerify(exactly = 1) { repository.generateResults("1") }
    }

    @Test
    fun `ApproveResultsUseCase returns the approval status from the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        val status = ResultApprovalStatus("1", "ADMIN", false, "Looks good")
        coEvery { repository.approveResults("1", "ADMIN", "Looks good") } returns status

        val result = ApproveResultsUseCase(repository)("1", "ADMIN", "Looks good")

        assertEquals("1", result.scheduleId)
        assertEquals("ADMIN", result.level)
        assertEquals(false, result.chainComplete)
        assertEquals("Looks good", result.remarks)
    }

    @Test
    fun `RejectResultsUseCase forwards scheduleId, level and remarks`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.rejectResults("1", "ADMIN", "Marks mismatch") } just Runs

        RejectResultsUseCase(repository)("1", "ADMIN", "Marks mismatch")

        coVerify(exactly = 1) { repository.rejectResults("1", "ADMIN", "Marks mismatch") }
    }

    @Test
    fun `PublishResultsUseCase forwards scheduleId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.publishResults("1") } just Runs

        PublishResultsUseCase(repository)("1")

        coVerify(exactly = 1) { repository.publishResults("1") }
    }

    @Test
    fun `HoldResultPublicationUseCase forwards scheduleId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.holdResultPublication("1") } just Runs

        HoldResultPublicationUseCase(repository)("1")

        coVerify(exactly = 1) { repository.holdResultPublication("1") }
    }

    @Test
    fun `UnpublishResultsUseCase forwards scheduleId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.unpublishResults("1") } just Runs

        UnpublishResultsUseCase(repository)("1")

        coVerify(exactly = 1) { repository.unpublishResults("1") }
    }
}
