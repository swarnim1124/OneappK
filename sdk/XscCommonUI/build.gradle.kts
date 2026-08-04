plugins {
    id("oneapp.android.library")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.xsc.sdk.commonui"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":sdk:XscThemeSDK"))
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
}
