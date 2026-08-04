package com.xsc.oneapp.feature.dashboard.domain.usecase

import com.xsc.oneapp.feature.dashboard.domain.repository.DashboardRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class GetPinnedModuleIdsUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() {
        val repository = mockk<DashboardRepository>()
        every { repository.getPinnedModuleIds() } returns setOf("academics", "attendance")

        val result = GetPinnedModuleIdsUseCase(repository).invoke()

        assertEquals(setOf("academics", "attendance"), result)
    }
}
