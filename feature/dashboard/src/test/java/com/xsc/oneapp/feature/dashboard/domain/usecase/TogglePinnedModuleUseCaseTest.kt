package com.xsc.oneapp.feature.dashboard.domain.usecase

import com.xsc.oneapp.feature.dashboard.domain.repository.DashboardRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class TogglePinnedModuleUseCaseTest {

    @Test
    fun `pinning a module not already pinned adds it and persists the new set`() {
        val repository = mockk<DashboardRepository>()
        every { repository.getPinnedModuleIds() } returns setOf("academics")
        every { repository.setPinnedModuleIds(any()) } returns Unit

        val result = TogglePinnedModuleUseCase(repository).invoke("attendance")

        assertEquals(setOf("academics", "attendance"), result)
        verify { repository.setPinnedModuleIds(setOf("academics", "attendance")) }
    }

    @Test
    fun `toggling an already-pinned module removes it`() {
        val repository = mockk<DashboardRepository>()
        every { repository.getPinnedModuleIds() } returns setOf("academics", "attendance")
        every { repository.setPinnedModuleIds(any()) } returns Unit

        val result = TogglePinnedModuleUseCase(repository).invoke("academics")

        assertEquals(setOf("attendance"), result)
        verify { repository.setPinnedModuleIds(setOf("attendance")) }
    }
}
