package com.xsc.oneapp.core.observability

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

/**
 * Thin wrapper around Firebase Remote Config, same defensive shape as [CrashReporter]
 * for the same reason - see that file's kdoc.
 *
 * No flag names are defined here: nothing in this app branches on a remote flag yet,
 * so there is nothing real to declare defaults for. [getBoolean]/[getString] resolve
 * whatever the SDK itself would - which for a key with no fetched value is Firebase's
 * own built-in zero value (`false`/`""`), *not* the caller's [default] - [default] only
 * wins when Remote Config itself never became available (SDK init failed, or nothing
 * has been fetched yet on a fresh install with no cached values). A caller that needs
 * a real default for an unset key should call
 * `FirebaseRemoteConfig.getInstance().setDefaultsAsync(...)` once real flags exist,
 * rather than relying on this wrapper to fake that per-call.
 */
object RemoteConfigProvider {
    @Volatile
    private var isAvailable = false

    private val remoteConfig: FirebaseRemoteConfig
        get() = FirebaseRemoteConfig.getInstance()

    /** Call once, as early as possible (see OneAppApplication.onCreate). Fetch is
     * fire-and-forget - callers read whatever's already cached/activated; nothing
     * here blocks app startup on a network round trip. */
    fun init() {
        isAvailable = try {
            remoteConfig.setConfigSettingsAsync(
                FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(MINIMUM_FETCH_INTERVAL_SECONDS)
                    .build()
            )
            remoteConfig.fetchAndActivate()
                .addOnFailureListener { e -> CrashReporter.recordException(e) }
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        if (!isAvailable) return default
        return try {
            remoteConfig.getBoolean(key)
        } catch (e: Throwable) {
            default
        }
    }

    fun getString(key: String, default: String): String {
        if (!isAvailable) return default
        return try {
            remoteConfig.getString(key)
        } catch (e: Throwable) {
            default
        }
    }

    /** Firebase's own recommended default outside of active A/B testing - frequent
     * enough that a flag flip reaches devices same-day, not so frequent it's a
     * meaningful fraction of the app's own network traffic. */
    private const val MINIMUM_FETCH_INTERVAL_SECONDS = 3600L
}
