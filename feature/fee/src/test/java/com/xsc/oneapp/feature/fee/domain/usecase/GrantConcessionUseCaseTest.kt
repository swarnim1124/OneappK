package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GrantConcessionUseCaseTest {

    @Test
    fun `invoke forwards every field to the repository`() = runTest {
        val repository = mockk<FeeRepository>()
        coEvery { repository.grantConcession("10", "1", "MERIT_SCHOLARSHIP", 5000.0, "Top rank") } just Runs

        GrantConcessionUseCase(repository)("10", "1", "MERIT_SCHOLARSHIP", 5000.0, "Top rank")

        coVerify(exactly = 1) { repository.grantConcession("10", "1", "MERIT_SCHOLARSHIP", 5000.0, "Top rank") }
    }
}
