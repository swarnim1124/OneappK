package com.xsc.oneapp.di

import com.xsc.oneapp.core.registry.ModuleDefinition
import com.xsc.oneapp.core.registry.ModuleRegistry
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import javax.inject.Singleton

/**
 * Aggregates every feature module's `@Provides @IntoSet ModuleDefinition` contribution
 * into one injectable [ModuleRegistry]. Mirrors DashboardModule's
 * bindDashboardStatProviders exactly - the `@Multibinds` declaration exists so
 * `Set<ModuleDefinition>` resolves even if a future feature module is added before it
 * contributes its own definition, instead of failing to compile/inject.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ModuleRegistryModule {

    @Multibinds
    abstract fun bindModuleDefinitions(): Set<ModuleDefinition>

    companion object {
        @Provides
        @Singleton
        fun provideModuleRegistry(definitions: Set<@JvmSuppressWildcards ModuleDefinition>): ModuleRegistry =
            ModuleRegistry(definitions)
    }
}
