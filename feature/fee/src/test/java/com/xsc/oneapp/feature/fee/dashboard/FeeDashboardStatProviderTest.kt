package com.xsc.oneapp.feature.fee.dashboard

import com.xsc.oneapp.feature.fee.domain.model.FeeStatement
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import com.xsc.sdk.network.APIError
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FeeDashboardStatProviderTest {

    @Test
    fun `statId matches the Dashboard's fees placeholder`() {
        val provider = FeeDashboardStatProvider(mockk())
        assertEquals("fees", provider.statId)
    }

    @Test
    fun `an outstanding balance is surfaced as a real rupee amount`() = runTest {
        val repository = mockk<FeeRepository>()
        coEvery { repository.getMyFeeStatement() } returns
            FeeStatement("12045", 50000.0, 10000.0, 40000.0)

        val contribution = FeeDashboardStatProvider(repository).provideStat()

        assertEquals("fees", contribution?.id)
        assertEquals("₹40,000", contribution?.value)
        assertEquals(FeeDashboardStatProvider.TAG_DUE, contribution?.tag)
    }

    @Test
    fun `a fully paid statement reports zero due rather than falling back to a placeholder`() = runTest {
        val repository = mockk<FeeRepository>()
        coEvery { repository.getMyFeeStatement() } returns
            FeeStatement("12045", 50000.0, 50000.0, 0.0)

        val contribution = FeeDashboardStatProvider(repository).provideStat()

        assertEquals("₹0", contribution?.value)
        assertEquals(FeeDashboardStatProvider.TAG_PAID, contribution?.tag)
    }

    @Test
    fun `an empty statement leaves the Dashboard's own placeholder untouched`() = runTest {
        val repository = mockk<FeeRepository>()
        coEvery { repository.getMyFeeStatement() } returns FeeStatement.EMPTY

        val contribution = FeeDashboardStatProvider(repository).provideStat()

        assertNull(contribution)
    }

    @Test
    fun `a failed fetch leaves the Dashboard's own placeholder untouched rather than crashing it`() = runTest {
        val repository = mockk<FeeRepository>()
        coEvery { repository.getMyFeeStatement() } throws APIError.NetworkError("offline")

        val contribution = FeeDashboardStatProvider(repository).provideStat()

        assertNull(contribution)
    }
}
