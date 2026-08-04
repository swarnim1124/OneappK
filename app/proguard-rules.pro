# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Kept (not the commented-out IDE default) because Crashlytics needs real
# stack traces to be useful once C-1/H-8 land - see the Crashlytics section
# below. `-renamesourcefileattribute` still strips the actual source path,
# it only keeps the file:line pair the mapping file re-expands.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------
# Gson (reflection-based JSON (de)serialization)
# ---------------------------------------------------------------------------
# Every OneApp network call goes through the single dispatcher envelope
# (DispatchRequest / DispatchResponse, see sdk/XscNetworkSDK) and Gson reads/
# writes model fields by name via reflection. Without these rules R8 renames
# or strips fields and Gson silently produces all-null objects instead of a
# build failure - this is the #1 way enabling R8 late breaks a Gson codebase.

# Keep generic signatures and annotations - required for Gson's TypeToken
# handling and for Retrofit's generic Call<T>/Response<T> types.
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses, RuntimeVisibleAnnotations

# Keep every field Gson maps by name (@SerializedName or not - Gson's default
# field-name lookup also breaks if the field itself is renamed).
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# The wire-format DTOs. Every feature keeps them isolated to
# `data.remote.dto` (login, profile, dashboard today; keeping the whole
# package for any future feature that follows the same convention).
-keep class com.xsc.oneapp.**.data.remote.dto.** { *; }
-keepclassmembers class com.xsc.oneapp.**.data.remote.dto.** { *; }

# The single request/response envelope every dispatcher call speaks.
-keep class com.xsc.sdk.network.api.DispatchRequest { *; }
-keep class com.xsc.sdk.network.api.DispatchResponse { *; }
-keepclassmembers class com.xsc.sdk.network.api.DispatchRequest { *; }
-keepclassmembers class com.xsc.sdk.network.api.DispatchResponse { *; }

# Gson's own reflective access to TypeAdapters/TypeToken.
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# ---------------------------------------------------------------------------
# Retrofit / OkHttp
# ---------------------------------------------------------------------------
# Modern Retrofit/OkHttp ship their own consumer-proguard-rules, but the app
# only has a single hand-rolled Retrofit interface (DispatcherApi) - keep it
# explicitly rather than relying on transitive library defaults matching it.
-keep interface com.xsc.sdk.network.internal.DispatcherApi { *; }

-keepattributes Exceptions
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ---------------------------------------------------------------------------
# EncryptedSharedPreferences / Tink (SessionManager's token storage)
# ---------------------------------------------------------------------------
# Tink registers its AEAD/key-management primitives via reflection at
# startup; over-aggressive shrinking here is a documented way to turn
# MasterKey.build() into a startup crash loop (see PRODUCTION_READINESS_FINAL
# risk #10 - this is exactly the failure mode R8 can introduce if untreated).
-keep class com.google.crypto.tink.** { *; }
-keep interface com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**

# ---------------------------------------------------------------------------
# Kotlin / coroutines / Hilt
# ---------------------------------------------------------------------------
# Hilt/Dagger-generated factories reference every injected type directly
# (no reflection), so no manual @HiltViewModel keep rule is needed - but
# Kotlin's coroutine internals and metadata are still reflection-adjacent
# enough that stripping warnings here would hide real problems, not fix them.
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlin.Metadata { *; }
