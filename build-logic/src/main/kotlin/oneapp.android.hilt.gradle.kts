import org.gradle.api.artifacts.VersionCatalogsExtension

apply(plugin = "org.jetbrains.kotlin.kapt")
apply(plugin = "com.google.dagger.hilt.android")

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    add("implementation", libs.findLibrary("hilt-android").get())
    add("kapt", libs.findLibrary("hilt-android-compiler").get())
}
