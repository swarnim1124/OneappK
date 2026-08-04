package com.xsc.oneapp.feature.dashboard.domain.usecase

import com.xsc.oneapp.feature.dashboard.domain.model.ModuleItem
import com.xsc.oneapp.feature.dashboard.domain.repository.DashboardRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAccessibleModulesUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<DashboardRepository>()
        val modules = listOf(
            ModuleItem("academics", "Curriculum", "school", "/academics", ModuleItem.ModuleStatus.ACTIVE, "#4F46E5"),
            ModuleItem("exams", "Exams", "description", "/exams", ModuleItem.ModuleStatus.ACTIVE, "#EF4444"),
            ModuleItem("fees", "Fees", "currency_rupee", "/fees", ModuleItem.ModuleStatus.ACTIVE, "#06B6D4")
        )
        coEvery { repository.getAccessibleModules() } returns modules

        val result = GetAccessibleModulesUseCase(repository).invoke()

        assertEquals(modules, result)
    }
}
