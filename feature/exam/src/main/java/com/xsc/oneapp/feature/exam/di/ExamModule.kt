package com.xsc.oneapp.feature.exam.di

import com.xsc.oneapp.feature.exam.data.repository.ExamRepositoryImpl
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExamModule {

    @Binds
    @Singleton
    abstract fun bindExamRepository(impl: ExamRepositoryImpl): ExamRepository
}
