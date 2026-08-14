package com.xsc.oneapp.feature.profile.di

import com.xsc.oneapp.core.registry.ModuleDefinition
import com.xsc.oneapp.feature.profile.data.remote.datasource.ProfileRemoteDataSource
import com.xsc.oneapp.feature.profile.data.remote.datasource.ProfileRemoteDataSourceImpl
import com.xsc.oneapp.feature.profile.data.repository.ProfileRepositoryImpl
import com.xsc.oneapp.feature.profile.domain.repository.ProfileRepository
import com.xsc.oneapp.feature.profile.navigation.ProfileDestinations
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    @Singleton
    abstract fun bindProfileRemoteDataSource(
        impl: ProfileRemoteDataSourceImpl
    ): ProfileRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository

    companion object {
        /** See ModuleDefinition's doc comment for what this deliberately does and
         * doesn't own - navigationEntry mirrors this module's own Destinations
         * object directly since ProfileModule already lives alongside it. */
        @Provides
        @IntoSet
        fun provideProfileModuleDefinition(): ModuleDefinition = ModuleDefinition(
            moduleId = "profile",
            displayName = "Profile",
            navigationEntry = ProfileDestinations.PROFILE_ROUTE,
            routeAliases = setOf("profile")
        )
    }
}
