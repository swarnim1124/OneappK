package com.xsc.oneapp.feature.timetable.di

import com.xsc.oneapp.core.registry.ModuleDefinition
import com.xsc.oneapp.feature.timetable.data.repository.TimetableRepositoryImpl
import com.xsc.oneapp.feature.timetable.domain.repository.TimetableRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TimetableModule {

    @Binds
    @Singleton
    abstract fun bindTimetableRepository(impl: TimetableRepositoryImpl): TimetableRepository

    companion object {
        /** See ExamModule's equivalent comment - navigationEntry/routeAliases must
         * match Routes.TIMETABLE's value ("timetable") exactly, duplicated by
         * necessity. */
        @Provides
        @IntoSet
        fun provideTimetableModuleDefinition(): ModuleDefinition = ModuleDefinition(
            moduleId = "timetable",
            displayName = "Timetable",
            navigationEntry = "timetable",
            routeAliases = setOf("timetable")
        )
    }
}
