pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "OneApp"

include(":app")
include(":core")

include(":sdk:XscNetworkSDK")
include(":sdk:XscAuthSDK")
include(":sdk:XscThemeSDK")
include(":sdk:XscStorageSDK")
include(":sdk:XscCommonUI")
include(":sdk:XscAnalyticsSDK")
include(":sdk:XscNotificationSDK")
include(":sdk:XscMediaSDK")
include(":sdk:XscCameraSDK")
include(":sdk:XscQRCodeSDK")
include(":sdk:XscFileSDK")
include(":sdk:XscChatSDK")

include(":feature:login")
include(":feature:dashboard")
include(":feature:profile")
include(":feature:exam")
include(":feature:attendance")
include(":feature:curriculum")
include(":feature:timetable")
include(":feature:fee")
