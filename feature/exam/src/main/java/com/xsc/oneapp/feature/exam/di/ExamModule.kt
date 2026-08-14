package com.xsc.oneapp.feature.exam.di

import com.xsc.oneapp.core.registry.ModuleDefinition
import com.xsc.oneapp.feature.exam.data.repository.ExamRepositoryImpl
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import com.xsc.oneapp.feature.exam.navigation.ExamDestinations
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExamModule {

    @Binds
    @Singleton
    abstract fun bindExamRepository(impl: ExamRepositoryImpl): ExamRepository

    companion object {
        /** See ModuleDefinition's doc comment for what this deliberately does and
         * doesn't own. navigationEntry mirrors this module's own Destinations object
         * (see AttendanceModule's identical precedent) rather than a bare literal, now
         * that this feature owns a nested graph instead of one flat :app composable.
         * routeAliases stay the literal backend-contract route strings ("exams"/"exam") -
         * those come from m_exam's route field, not from this module's internal
         * navigation, so they don't change when the internal graph route does. */
        @Provides
        @IntoSet
        fun provideExamModuleDefinition(): ModuleDefinition = ModuleDefinition(
            moduleId = "exams",
            displayName = "Exams",
            navigationEntry = ExamDestinations.GRAPH_ROUTE,
            routeAliases = setOf("exams", "exam")
        )
    }
}
