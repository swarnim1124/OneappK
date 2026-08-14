package com.xsc.oneapp.feature.fee.di

import com.xsc.oneapp.core.dashboard.DashboardStatProvider
import com.xsc.oneapp.core.registry.ModuleDefinition
import com.xsc.oneapp.feature.fee.dashboard.FeeDashboardStatProvider
import com.xsc.oneapp.feature.fee.data.repository.FeeRepositoryImpl
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FeeModule {

    @Binds
    @Singleton
    abstract fun bindFeeRepository(impl: FeeRepositoryImpl): FeeRepository

    @Binds
    @IntoSet
    abstract fun bindFeeDashboardStatProvider(impl: FeeDashboardStatProvider): DashboardStatProvider

    companion object {
        /** See ExamModule's equivalent comment - navigationEntry/routeAliases must
         * match Routes.FEES's value ("fees") exactly, duplicated by necessity since
         * this module can't reference :app's Routes directly. */
        @Provides
        @IntoSet
        fun provideFeeModuleDefinition(): ModuleDefinition = ModuleDefinition(
            moduleId = "fees",
            displayName = "Fees",
            navigationEntry = "fees",
            routeAliases = setOf("fees", "fee")
        )
    }
}
