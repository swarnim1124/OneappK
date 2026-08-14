package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FeeRefundUseCasesTest {

    @Test
    fun `RequestRefundUseCase forwards invoiceId, amount and reason`() = runTest {
        val repository = mockk<FeeRepository>()
        coEvery { repository.requestRefund("1", 2000.0, "Overpayment during admission") } just Runs

        RequestRefundUseCase(repository)("1", 2000.0, "Overpayment during admission")

        coVerify(exactly = 1) { repository.requestRefund("1", 2000.0, "Overpayment during admission") }
    }

    @Test
    fun `UpdateRefundStatusUseCase forwards refundId, status and remarks`() = runTest {
        val repository = mockk<FeeRepository>()
        coEvery { repository.updateRefundStatus("1", "APPROVED", "Verified") } just Runs

        UpdateRefundStatusUseCase(repository)("1", "APPROVED", "Verified")

        coVerify(exactly = 1) { repository.updateRefundStatus("1", "APPROVED", "Verified") }
    }
}
