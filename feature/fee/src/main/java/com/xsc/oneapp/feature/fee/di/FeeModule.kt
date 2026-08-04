package com.xsc.oneapp.feature.fee.di

import com.xsc.oneapp.feature.fee.data.repository.FeeRepositoryImpl
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FeeModule {

    @Binds
    @Singleton
    abstract fun bindFeeRepository(impl: FeeRepositoryImpl): FeeRepository
}
