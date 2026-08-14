package com.xsc.oneapp.feature.curriculum.di

import com.xsc.oneapp.core.registry.ModuleDefinition
import com.xsc.oneapp.feature.curriculum.data.repository.CurriculumRepositoryImpl
import com.xsc.oneapp.feature.curriculum.domain.repository.CurriculumRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CurriculumModule {

    @Binds
    @Singleton
    abstract fun bindCurriculumRepository(impl: CurriculumRepositoryImpl): CurriculumRepository

    companion object {
        /** See ExamModule's equivalent comment - navigationEntry must match
         * Routes.CURRICULUM's value ("curriculum") exactly, duplicated by necessity.
         * routeAliases keeps both "academics" (the m_AAA contract id) and
         * "curriculum" (the newer name), same as Routes.destinationFor accepted
         * before this refactor. */
        @Provides
        @IntoSet
        fun provideCurriculumModuleDefinition(): ModuleDefinition = ModuleDefinition(
            moduleId = "curriculum",
            displayName = "Curriculum",
            navigationEntry = "curriculum",
            routeAliases = setOf("academics", "curriculum")
        )
    }
}
