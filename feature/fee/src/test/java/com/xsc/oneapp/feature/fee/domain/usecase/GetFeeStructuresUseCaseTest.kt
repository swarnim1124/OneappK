package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.model.FeeStructure
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetFeeStructuresUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<FeeRepository>()
        val structures = listOf(
            FeeStructure("1", "FS_2026_BTECH", "B.Tech Fall 2026 Tuition", "Standard tuition", "101", "5", "2026-08-01", null, "1", "true")
        )
        coEvery { repository.getFeeStructures() } returns structures

        val result = GetFeeStructuresUseCase(repository).invoke()

        assertEquals(structures, result)
    }
}
