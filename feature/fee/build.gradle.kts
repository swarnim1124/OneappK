import java.util.Properties

plugins {
    id("oneapp.android.library")
    id("oneapp.android.hilt")
    alias(libs.plugins.kotlin.compose)
}

// Razorpay test key_id, read from a gitignored `razorpay.properties` at the repo
// root (see razorpay.properties.example) or the RAZORPAY_KEY_ID env var for CI -
// same pattern app/build.gradle.kts already uses for release signing material.
// Lives here (not in :app) because this module is what actually calls
// Checkout().open(activity, options) - :app only implements the SDK's required
// callback interface, it never reads the key itself. Defaults to empty string so
// the checkout path can show an honest "not configured yet" state instead of
// calling the SDK with nothing to initialize against.
val razorpayProperties = Properties().apply {
    val propsFile = rootProject.file("razorpay.properties")
    if (propsFile.exists()) {
        propsFile.inputStream().use { load(it) }
    }
}
val razorpayKeyId = razorpayProperties.getProperty("keyId")?.takeIf { it.isNotBlank() }
    ?: System.getenv("RAZORPAY_KEY_ID")?.takeIf { it.isNotBlank() }
    ?: ""

android {
    namespace = "com.xsc.oneapp.feature.fee"

    defaultConfig {
        buildConfigField("String", "VERSION_NAME", "\"1.0\"")
        buildConfigField("String", "RAZORPAY_KEY_ID", "\"$razorpayKeyId\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":sdk:XscNetworkSDK"))
    implementation(project(":sdk:XscAuthSDK"))
    implementation(project(":sdk:XscThemeSDK"))
    implementation(project(":sdk:XscCommonUI"))

    implementation(libs.razorpay.checkout)
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.gson)

    implementation(libs.hilt.navigation.compose)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
