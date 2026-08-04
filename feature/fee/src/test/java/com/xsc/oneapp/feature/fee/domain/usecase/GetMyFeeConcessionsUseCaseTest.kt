package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.model.FeeConcession
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetMyFeeConcessionsUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<FeeRepository>()
        val concessions = listOf(
            FeeConcession("302", "12045", "2", "2000.0", null, "Top 10% Entrance Exam Rank", "1", "992", "2026-08-15T14:30:00Z")
        )
        coEvery { repository.getMyFeeConcessions() } returns concessions

        val result = GetMyFeeConcessionsUseCase(repository).invoke()

        assertEquals(concessions, result)
    }
}
