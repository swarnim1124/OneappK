plugins {
    id("oneapp.android.library")
    id("oneapp.android.hilt")
}

// Override at build time instead of editing source, e.g.:
//   ./gradlew assembleRelease -PbaseUrl=https://staging.globaloneapp.com/ -PrecaptchaSiteKey=...
// Defaults preserve today's behavior exactly for debug: the dev deployment every
// *_API_CONTRACT.md document references, and an empty reCAPTCHA key (safe no-op -
// see RecaptchaManager).
val explicitBaseUrl = project.findProperty("baseUrl") as String?
val baseUrl = explicitBaseUrl ?: "https://dev.globaloneapp.com/"
val recaptchaSiteKey = (project.findProperty("recaptchaSiteKey") as String?) ?: ""

// PRODUCTION_READINESS_AUDIT.md C-5: a plain `./gradlew assembleRelease` used to
// silently produce a release build still pointed at the dev server, because this
// default applied to every build type equally. Refuse to build a release variant
// unless `-PbaseUrl=...` (or CI's equivalent property injection) was passed
// explicitly - debug is untouched.
val isBuildingReleaseVariant = gradle.startParameter.taskNames.any { taskName ->
    taskName.contains("Release")
}
if (isBuildingReleaseVariant && explicitBaseUrl.isNullOrBlank()) {
    throw GradleException(
        "Refusing to build a release variant with the default dev base URL " +
            "($baseUrl). Pass -PbaseUrl=<the real staging/production URL> explicitly."
    )
}

android {
    namespace = "com.xsc.sdk.network"

    defaultConfig {
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        buildConfigField("String", "RECAPTCHA_SITE_KEY", "\"$recaptchaSiteKey\"")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":sdk:XscAuthSDK"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    api(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)
    api(libs.gson)

    implementation(libs.play.services.recaptcha)
}
