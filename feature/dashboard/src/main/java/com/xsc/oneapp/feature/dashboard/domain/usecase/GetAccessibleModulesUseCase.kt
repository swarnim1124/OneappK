package com.xsc.oneapp.feature.dashboard.domain.usecase

import com.xsc.oneapp.feature.dashboard.domain.model.ModuleItem
import com.xsc.oneapp.feature.dashboard.domain.repository.DashboardRepository
import javax.inject.Inject

class GetAccessibleModulesUseCase @Inject constructor(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(): List<ModuleItem> = repository.getAccessibleModules()
}
