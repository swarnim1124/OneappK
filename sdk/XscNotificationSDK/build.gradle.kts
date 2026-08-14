plugins {
    id("oneapp.android.library")
}

android {
    namespace = "com.xsc.sdk.notification"
}

dependencies {
    // androidx-core-ktx already carries NotificationCompat/NotificationManagerCompat -
    // no separate androidx.core dependency needed for local notification display.
    implementation(libs.androidx.core.ktx)

    // Compiles and links regardless of whether google-services.json exists yet - see
    // CrashReporter.kt (:core) for the same pattern. FirebaseMessagingService itself
    // only fails to receive real messages until Firebase actually initializes.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
}
