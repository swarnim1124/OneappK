package com.xsc.oneapp.feature.timetable.di

import com.xsc.oneapp.feature.timetable.data.repository.TimetableRepositoryImpl
import com.xsc.oneapp.feature.timetable.domain.repository.TimetableRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TimetableModule {

    @Binds
    @Singleton
    abstract fun bindTimetableRepository(impl: TimetableRepositoryImpl): TimetableRepository
}
