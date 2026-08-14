// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.hilt) apply false
    // Declared here (apply false) so the version resolves for :app, the only module
    // that applies any of these three (Performance Monitoring's Gradle plugin only
    // supports application modules - see app/build.gradle.kts for the
    // google-services.json precondition all three share).
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics.plugin) apply false
    alias(libs.plugins.firebase.perf.plugin) apply false
}
