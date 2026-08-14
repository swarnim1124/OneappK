package com.xsc.oneapp.core.observability

import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Thin wrapper around Crashlytics so call sites (see [com.xsc.oneapp.core.result.uiStateCatching])
 * never need to know whether Firebase is actually initialized.
 *
 * `app/google-services.json` now registers the real package (`com.xsc.oneapp`) against
 * a real Firebase project, so this reports for real - the wrapper stays defensive
 * anyway (see [isAvailable]) since a gitignored, locally-supplied config file is the
 * kind of thing a fresh checkout can still be missing, and crash reporting must never
 * be the thing that crashes the app.
 */
object CrashReporter {
    @Volatile
    private var isAvailable = false

    /** Call once, as early as possible (see OneAppApplication.onCreate). */
    fun init() {
        isAvailable = try {
            FirebaseCrashlytics.getInstance()
            true
        } catch (e: Throwable) {
            false
        }
    }

    /** Non-fatal - the app already recovered (or is about to show an error state);
     * this only makes the failure visible in aggregate instead of only on the one
     * device it happened on. */
    fun recordException(throwable: Throwable) {
        if (!isAvailable) return
        try {
            FirebaseCrashlytics.getInstance().recordException(throwable)
        } catch (e: Throwable) {
            // Crash reporting must never be the thing that crashes the app.
        }
    }

    /** Breadcrumb attached to the next report from this device, fatal or not. */
    fun log(message: String) {
        if (!isAvailable) return
        try {
            FirebaseCrashlytics.getInstance().log(message)
        } catch (e: Throwable) {
        }
    }

    /** Associates subsequent reports with a user without sending anything
     * personally identifying - callers should pass an opaque id, e.g.
     * SessionManager's numeric userId, never an email. */
    fun setUserId(userId: String?) {
        if (!isAvailable) return
        try {
            FirebaseCrashlytics.getInstance().setUserId(userId.orEmpty())
        } catch (e: Throwable) {
        }
    }
}
