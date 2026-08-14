package com.xsc.oneapp.feature.attendance.di

import com.xsc.oneapp.core.dashboard.DashboardStatProvider
import com.xsc.oneapp.core.registry.ModuleDefinition
import com.xsc.oneapp.feature.attendance.dashboard.AttendanceDashboardStatProvider
import com.xsc.oneapp.feature.attendance.data.repository.AttendanceRepositoryImpl
import com.xsc.oneapp.feature.attendance.domain.repository.AttendanceRepository
import com.xsc.oneapp.feature.attendance.navigation.AttendanceDestinations
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AttendanceModule {

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(impl: AttendanceRepositoryImpl): AttendanceRepository

    @Binds
    @IntoSet
    abstract fun bindAttendanceDashboardStatProvider(impl: AttendanceDashboardStatProvider): DashboardStatProvider

    companion object {
        /** See ModuleDefinition's doc comment for what this deliberately does and
         * doesn't own - navigationEntry mirrors this module's own Destinations
         * object directly since AttendanceModule already lives alongside it. */
        @Provides
        @IntoSet
        fun provideAttendanceModuleDefinition(): ModuleDefinition = ModuleDefinition(
            moduleId = "attendance",
            displayName = "Attendance",
            navigationEntry = AttendanceDestinations.GRAPH_ROUTE,
            routeAliases = setOf("attendance")
        )
    }
}
