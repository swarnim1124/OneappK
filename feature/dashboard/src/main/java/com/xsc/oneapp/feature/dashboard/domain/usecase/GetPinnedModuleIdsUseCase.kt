package com.xsc.oneapp.feature.dashboard.domain.usecase

import com.xsc.oneapp.feature.dashboard.domain.repository.DashboardRepository
import javax.inject.Inject

class GetPinnedModuleIdsUseCase @Inject constructor(
    private val repository: DashboardRepository
) {
    operator fun invoke(): Set<String> = repository.getPinnedModuleIds()
}
