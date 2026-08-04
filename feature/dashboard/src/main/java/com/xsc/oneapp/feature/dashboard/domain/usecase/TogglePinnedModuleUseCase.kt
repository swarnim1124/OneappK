package com.xsc.oneapp.feature.dashboard.domain.usecase

import com.xsc.oneapp.feature.dashboard.domain.repository.DashboardRepository
import javax.inject.Inject

class TogglePinnedModuleUseCase @Inject constructor(
    private val repository: DashboardRepository
) {
    operator fun invoke(moduleId: String): Set<String> {
        val current = repository.getPinnedModuleIds()
        val updated = if (moduleId in current) current - moduleId else current + moduleId
        repository.setPinnedModuleIds(updated)
        return updated
    }
}
