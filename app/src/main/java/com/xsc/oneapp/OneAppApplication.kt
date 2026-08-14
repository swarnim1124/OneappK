package com.xsc.oneapp

import android.app.Application
import com.xsc.oneapp.core.observability.CrashReporter
import com.xsc.oneapp.core.observability.RemoteConfigProvider
import com.xsc.sdk.notification.PushNotifications
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OneAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Safe to call unconditionally - see CrashReporter's/RemoteConfigProvider's
        // own kdoc for why each is a no-op until app/google-services.json (registering
        // package com.xsc.oneapp) is in place - see app/build.gradle.kts.
        CrashReporter.init()
        RemoteConfigProvider.init()
        // Channel creation itself has no Firebase dependency (Android's
        // NotificationManager, not FCM) - always safe to call regardless of whether
        // google-services.json exists yet.
        PushNotifications.createChannel(this)
    }
}
