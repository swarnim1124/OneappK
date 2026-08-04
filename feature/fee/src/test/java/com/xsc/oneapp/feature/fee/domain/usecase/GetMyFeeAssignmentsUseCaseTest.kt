package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.model.FeeAssignment
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetMyFeeAssignmentsUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<FeeRepository>()
        val assignments = listOf(
            FeeAssignment("5001", "12045", "1", "10", "15000.0", "2026-09-01", "1", "true")
        )
        coEvery { repository.getMyFeeAssignments() } returns assignments

        val result = GetMyFeeAssignmentsUseCase(repository).invoke()

        assertEquals(assignments, result)
    }
}
