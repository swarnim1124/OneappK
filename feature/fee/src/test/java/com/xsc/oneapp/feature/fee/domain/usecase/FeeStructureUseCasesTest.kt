package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.model.FeeStructureComponent
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FeeStructureUseCasesTest {

    @Test
    fun `CreateFeeStructureUseCase delegates every field to the repository`() = runTest {
        val repository = mockk<FeeRepository>()
        val component = FeeStructureComponent("TUIT", "Tuition Fee", 50000.0, false)
        coEvery {
            repository.createFeeStructure("FS-2026-ENG", "B.Tech Tuition", "1", "101", listOf(component), "2026-06-01", "2027-05-31")
        } returns "1"

        val result = CreateFeeStructureUseCase(repository)(
            "FS-2026-ENG", "B.Tech Tuition", "1", "101", listOf(component), "2026-06-01", "2027-05-31"
        )

        assertEquals("1", result)
    }

    @Test
    fun `UpdateFeeStructureStatusUseCase forwards structureId and status`() = runTest {
        val repository = mockk<FeeRepository>()
        coEvery { repository.updateFeeStructureStatus("1", "ACTIVE") } just Runs

        UpdateFeeStructureStatusUseCase(repository)("1", "ACTIVE")

        coVerify(exactly = 1) { repository.updateFeeStructureStatus("1", "ACTIVE") }
    }

    @Test
    fun `DeleteFeeStructureUseCase forwards structureId`() = runTest {
        val repository = mockk<FeeRepository>()
        coEvery { repository.deleteFeeStructure("1") } just Runs

        DeleteFeeStructureUseCase(repository)("1")

        coVerify(exactly = 1) { repository.deleteFeeStructure("1") }
    }
}
