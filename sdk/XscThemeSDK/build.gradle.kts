plugins {
    id("oneapp.android.library")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.xsc.sdk.theme"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
}
