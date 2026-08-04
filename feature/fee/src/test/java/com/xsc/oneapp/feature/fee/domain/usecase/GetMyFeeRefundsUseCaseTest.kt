package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.model.FeeRefund
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetMyFeeRefundsUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<FeeRepository>()
        val refunds = listOf(
            FeeRefund("105", "4110", "1000.0", "Accidental double payment via gateway", "2026-08-22T09:00:00Z", "1", "441")
        )
        coEvery { repository.getMyFeeRefunds() } returns refunds

        val result = GetMyFeeRefundsUseCase(repository).invoke()

        assertEquals(refunds, result)
    }
}
