package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.model.PaymentOrder
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PayFeeOnlineUseCaseTest {

    @Test
    fun `createOnlinePaymentOrder passes the statement amount straight through`() = runTest {
        val repository = mockk<FeeRepository>()
        val order = PaymentOrder("order_ABC123", 4_000_000L, "INR", null)
        coEvery { repository.createOnlinePaymentOrder("1", 40000.0, "Asha") } returns order

        val result = CreateOnlinePaymentOrderUseCase(repository).invoke("1", 40000.0, "Asha")

        assertEquals(order, result)
        coVerify(exactly = 1) { repository.createOnlinePaymentOrder("1", 40000.0, "Asha") }
    }

    @Test
    fun `confirmOnlinePayment surfaces an unreconciled payment as false, not an exception`() =
        runTest {
            val repository = mockk<FeeRepository>()
            coEvery { repository.confirmOnlinePayment("pay_X", true) } returns false

            assertFalse(ConfirmOnlinePaymentUseCase(repository).invoke("pay_X", true))
        }
}
