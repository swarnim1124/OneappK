package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.model.FeePayment
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetMyFeePaymentsUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<FeeRepository>()
        val payments = listOf(
            FeePayment("4110", "12045", "5", "ONLINE", "13000.0", "2026-08-20T10:15:00Z", "pay_XYZ123ABC", "1")
        )
        coEvery { repository.getMyFeePayments() } returns payments

        val result = GetMyFeePaymentsUseCase(repository).invoke()

        assertEquals(payments, result)
    }
}
