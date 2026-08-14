package com.xsc.oneapp.core.result

/**
 * Human-readable, user-facing error taxonomy. Every case here carries a specific,
 * actionable message - [Unknown] with its default text is the only generic fallback
 * in the app, reserved for exceptions that genuinely don't fit anywhere else.
 *
 * Two layers on purpose:
 *  - [Network] and [Traced] are what a backend call can fail with - built once here
 *    and reused by any feature (see [com.xsc.sdk.network.APIError] for where the raw
 *    transport/HTTP failure comes from).
 *  - Screen-specific validation/auth cases (e.g. a login form's "Invalid email or
 *    password") are NOT modelled here - they're domain concepts owned by the feature
 *    that has them (see feature/login's own LoginError), not generic infrastructure.
 */
sealed class AppError {
    abstract val message: String

    /** Transport-level failure - the request never reached, or never returned from,
     * the server. Distinguished by cause so the message tells the user what to
     * actually try (check their connection vs. just wait and retry). */
    sealed class Network : AppError() {
        data object NoConnection : Network() {
            override val message = "No internet connection. Check your network."
        }

        data object Unreachable : Network() {
            override val message = "Server is unreachable. Try again in a few minutes."
        }

        data object Timeout : Network() {
            override val message = "Request timed out. Please retry."
        }
    }

    /**
     * A backend call reached the server and failed with enough context to trace
     * exactly what was being attempted - built from the same
     * module/submodule/action/actionType tuple every dispatcher call already carries
     * (see [com.xsc.sdk.network.APIClient.request] and
     * [com.xsc.sdk.auth.PermissionKey.requiredPermissionFor]), so the message tells a
     * developer or admin exactly which permission to grant, or which record to look
     * up, without needing a debugger attached.
     *
     * [whatFailed] is the human action being attempted (e.g. "Could not load
     * profile") - only the call site knows this, not the network layer, so it's
     * supplied by whoever is turning the underlying [com.xsc.sdk.network.APIError]
     * into an [AppError.Traced].
     */
    data class Traced(
        val whatFailed: String,
        val httpCode: Int?,
        val why: String,
        val context: String
    ) : AppError() {
        override val message: String get() = "$whatFailed: [${httpCode?.toString() ?: "—"}] $why"

        /** Two-line form for a traced error card: reason on the first line, the
         * permission/endpoint/reference-id context on the second. */
        val displayMessage: String get() = "$message\n$context"
    }

    /** The one acceptable generic fallback - reserved for exceptions that were never
     * classified as a network or traced API failure. Never wraps a raw stack trace or
     * exception message; [raw] must already be user-facing copy. */
    data class Unknown(val raw: String = "Something went wrong. Try again.") : AppError() {
        override val message: String get() = raw
    }
}

/** Whether retrying the exact same request is worth offering. Network failures
 * always are; a traced failure only is when the code itself suggests a transient
 * condition (timeout, 5xx) rather than something retrying won't fix (403/404/422). */
val AppError.isRetryable: Boolean
    get() = when (this) {
        is AppError.Network -> true
        is AppError.Traced -> httpCode?.let { it == 408 || it >= 500 } ?: true
        is AppError.Unknown -> false
    }
