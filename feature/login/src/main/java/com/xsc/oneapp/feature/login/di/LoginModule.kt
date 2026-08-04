package com.xsc.oneapp.feature.login.di

import com.xsc.oneapp.feature.login.domain.repository.LoginRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LoginModule {

    @Binds
    @Singleton
    abstract fun bindLoginRepository(
        impl: com.xsc.oneapp.feature.login.data.repository.LoginRepositoryImpl
    ): LoginRepository
}
