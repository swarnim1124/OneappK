package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.model.FeeInvoice
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetMyFeeInvoicesUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<FeeRepository>()
        val invoices = listOf(
            FeeInvoice("89912", "12045", "4", "2026-08-05", "15000.0", "5001", "Tuition Fee Assignment Generation")
        )
        coEvery { repository.getMyFeeInvoices() } returns invoices

        val result = GetMyFeeInvoicesUseCase(repository).invoke()

        assertEquals(invoices, result)
    }
}
