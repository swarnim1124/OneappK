package com.xsc.sdk.network.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.xsc.sdk.network.BuildConfig
import com.xsc.sdk.network.NetworkConfig
import com.xsc.sdk.network.internal.AuthInterceptor
import com.xsc.sdk.network.internal.DispatcherApi
import com.xsc.sdk.network.internal.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @RecaptchaSiteKey
    fun provideRecaptchaSiteKey(): String = BuildConfig.RECAPTCHA_SITE_KEY

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            // Full request/response bodies (including credentials and the bearer
            // token AuthInterceptor attaches) are only useful - and only safe - in
            // debug builds. Release builds must never write this to Logcat.
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(NetworkConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NetworkConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NetworkConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        // No-op until NetworkConfig.CERTIFICATE_PINS has real pins - see its doc
        // comment. Only adds pinning (a fail-closed mechanism) once genuine pins
        // for the target host are available.
        if (NetworkConfig.CERTIFICATE_PINS.isNotEmpty()) {
            val pinner = CertificatePinner.Builder().apply {
                NetworkConfig.CERTIFICATE_PINS.forEach { (host, pins) ->
                    pins.forEach { pin -> add(host, pin) }
                }
            }.build()
            builder.certificatePinner(pinner)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideDispatcherApi(retrofit: Retrofit): DispatcherApi =
        retrofit.create(DispatcherApi::class.java)
}
