package com.xsc.oneapp.feature.dashboard.di

import javax.inject.Qualifier

/** SharedPreferences holding shell-local UI preferences (e.g. pinned modules) - not sensitive, not encrypted. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DashboardPrefs
