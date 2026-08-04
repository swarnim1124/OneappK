package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.model.FeePenalty
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetFeePenaltiesUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<FeeRepository>()
        val penalties = listOf(
            FeePenalty("88", "12045", "5001", "500.0", "Late payment beyond due date (09-01)", "2026-09-05", "1")
        )
        coEvery { repository.getFeePenalties() } returns penalties

        val result = GetFeePenaltiesUseCase(repository).invoke()

        assertEquals(penalties, result)
    }
}
