package com.xsc.oneapp

import android.app.Application
import com.xsc.oneapp.core.observability.CrashReporter
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OneAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Safe to call unconditionally - see CrashReporter's kdoc for why this is a
        // no-op until the google-services.json package mismatch is fixed and the
        // google-services/crashlytics Gradle plugins are uncommented in
        // app/build.gradle.kts.
        CrashReporter.init()
    }
}
