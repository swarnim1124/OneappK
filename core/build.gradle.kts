plugins {
    id("oneapp.android.library")
}

android {
    namespace = "com.xsc.oneapp.core"
}

dependencies {
    implementation(project(":sdk:XscNetworkSDK"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.gson)

    // Compiles and links regardless of whether the google-services/crashlytics
    // Gradle plugins are applied - those are only needed for config injection and
    // mapping-file upload, not for the SDK classes themselves. See CrashReporter.kt:
    // every call is guarded, so this is safe to have active before
    // app/google-services.json is fixed (see app/build.gradle.kts).
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
