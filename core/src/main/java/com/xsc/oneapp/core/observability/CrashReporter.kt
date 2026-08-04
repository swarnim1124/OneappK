package com.xsc.oneapp.core.observability

import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Thin wrapper around Crashlytics so call sites (see [com.xsc.oneapp.core.result.uiStateCatching])
 * never need to know whether Firebase is actually initialized.
 *
 * NOT FULLY ENABLED YET. `app/google-services.json` registers package
 * `swarnim.oneapp.com`, but this app's applicationId is `com.xsc.oneapp` - applying
 * the google-services/crashlytics Gradle plugins with that mismatch fails the build
 * outright (see app/build.gradle.kts, and PRODUCTION_READINESS_AUDIT.md C-1/H-8:
 * "you will be blind the moment R8 lands"). Both plugins stay commented out until the
 * Firebase console registers the right package and a corrected `google-services.json`
 * replaces the current one.
 *
 * The dependency itself doesn't need that fix to compile - only `FirebaseApp`'s
 * runtime auto-init does, since it reads the plugin-generated config resources. Until
 * then, [FirebaseCrashlytics.getInstance] throws `IllegalStateException` the first
 * time anything touches it; [isAvailable] catches that once at [init] and every other
 * method here becomes a no-op instead of every call site needing its own try/catch.
 * The moment the two plugins are uncommented and a valid config ships, this starts
 * reporting with zero further code changes.
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
