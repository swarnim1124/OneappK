package com.xsc.oneapp.feature.curriculum.di

import com.xsc.oneapp.feature.curriculum.data.repository.CurriculumRepositoryImpl
import com.xsc.oneapp.feature.curriculum.domain.repository.CurriculumRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CurriculumModule {

    @Binds
    @Singleton
    abstract fun bindCurriculumRepository(impl: CurriculumRepositoryImpl): CurriculumRepository
}
