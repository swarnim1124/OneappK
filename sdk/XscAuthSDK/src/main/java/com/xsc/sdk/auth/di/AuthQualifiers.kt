package com.xsc.sdk.auth.di

import javax.inject.Qualifier

/** Qualifies the [android.content.SharedPreferences] backing [com.xsc.sdk.auth.TokenManager]. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthPrefs
