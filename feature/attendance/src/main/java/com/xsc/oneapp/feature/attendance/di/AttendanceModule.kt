package com.xsc.oneapp.feature.attendance.di

import com.xsc.oneapp.feature.attendance.data.repository.AttendanceRepositoryImpl
import com.xsc.oneapp.feature.attendance.domain.repository.AttendanceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AttendanceModule {

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(impl: AttendanceRepositoryImpl): AttendanceRepository
}
