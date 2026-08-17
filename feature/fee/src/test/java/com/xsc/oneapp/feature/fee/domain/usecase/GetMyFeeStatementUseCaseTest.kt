package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.model.FeeStatement
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetMyFeeStatementUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<FeeRepository>()
        val statement = FeeStatement("12045", 50000.0, 10000.0, 40000.0)
        coEvery { repository.getMyFeeStatement() } returns statement

        val result = GetMyFeeStatementUseCase(repository).invoke()

        assertEquals(statement, result)
    }
}
