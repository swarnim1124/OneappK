package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FeeAssignmentUseCasesTest {

    @Test
    fun `AssignFeeUseCase forwards every field to the repository`() = runTest {
        val repository = mockk<FeeRepository>()
        coEvery { repository.assignFee("1", "10", "2026-07-15", listOf("10")) } just Runs

        AssignFeeUseCase(repository)("1", "10", "2026-07-15", listOf("10"))

        coVerify(exactly = 1) { repository.assignFee("1", "10", "2026-07-15", listOf("10")) }
    }

    @Test
    fun `DeleteFeeAssignmentUseCase forwards assignmentId`() = runTest {
        val repository = mockk<FeeRepository>()
        coEvery { repository.deleteFeeAssignment("5001") } just Runs

        DeleteFeeAssignmentUseCase(repository)("5001")

        coVerify(exactly = 1) { repository.deleteFeeAssignment("5001") }
    }
}
