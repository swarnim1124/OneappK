package com.xsc.oneapp.feature.dashboard.di

import android.content.Context
import android.content.SharedPreferences
import com.xsc.oneapp.core.dashboard.DashboardStatProvider
import com.xsc.oneapp.feature.dashboard.data.repository.DashboardRepositoryImpl
import com.xsc.oneapp.feature.dashboard.domain.repository.DashboardRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardModule {

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository

    /**
     * Declares the extension point even though no business module implements
     * [DashboardStatProvider] yet, so Set<DashboardStatProvider> resolves to an
     * empty set instead of failing to compile/inject. A feature module joins this
     * set by adding its own `@Binds @IntoSet` in its own Hilt module.
     */
    @Multibinds
    abstract fun bindDashboardStatProviders(): Set<DashboardStatProvider>

    companion object {
        @Provides
        @Singleton
        @DashboardPrefs
        fun provideDashboardPreferences(@ApplicationContext context: Context): SharedPreferences =
            context.getSharedPreferences("xsc_dashboard_prefs", Context.MODE_PRIVATE)
    }
}
