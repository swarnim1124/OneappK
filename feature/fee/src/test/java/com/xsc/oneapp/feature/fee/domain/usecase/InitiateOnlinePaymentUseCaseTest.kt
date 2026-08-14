package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.model.RazorpayOrder
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class InitiateOnlinePaymentUseCaseTest {

    @Test
    fun `invoke delegates to the repository and returns the order`() = runTest {
        val repository = mockk<FeeRepository>()
        val order = RazorpayOrder("order_MOCK12345678", 1000000, "INR")
        coEvery { repository.initiateOnlinePayment("1", 10000.0, "Student Self") } returns order

        val result = InitiateOnlinePaymentUseCase(repository)("1", 10000.0, "Student Self")

        assertEquals(order, result)
    }
}
