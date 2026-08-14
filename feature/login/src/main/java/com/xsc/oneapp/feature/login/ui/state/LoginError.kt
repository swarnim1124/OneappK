package com.xsc.oneapp.feature.login.ui.state

import com.xsc.oneapp.core.result.AppError
import com.xsc.oneapp.core.result.isRetryable

/**
 * Every error a user can hit on the sign-in screen, each with a specific,
 * actionable message shown as an inline banner or traced card (see LoginScreen) -
 * never a Toast that auto-dismisses.
 *
 * The six auth/validation cases are this screen's own domain, not generic
 * infrastructure - a different form would have its own specific set. [Network] and
 * [Traced] wrap the shared [AppError] taxonomy (see :core) for backend-driven
 * failures, and [Unknown] is the one acceptable generic fallback, reserved for
 * exceptions that don't fit anywhere else.
 */
sealed class LoginError {
    abstract val message: String
    open val isRetryable: Boolean = false

    data object IncompleteFields : LoginError() {
        override val message = "Please fill in all fields correctly."
    }

    /** Only raised when the input contains "@" but doesn't parse as an email - see
     * [com.xsc.oneapp.feature.login.ui.viewmodel.LoginViewModel]'s validation. The
     * sign-in field is labelled "Email or Username" and the backend payload key is
     * literally `username` (AAA_API_CONTRACT.md §3.1), so a bare username with no
     * "@" is valid input, not a format error. */
    data object InvalidEmailFormat : LoginError() {
        override val message = "Invalid email format."
    }

    data object InvalidCredentials : LoginError() {
        override val message = "Invalid email or password."
    }

    data object AccountLocked : LoginError() {
        override val message = "Account locked. Contact your administrator."
    }

    /** Shown once, on arrival, when [com.xsc.sdk.auth.TokenManager.sessionExpired]
     * is set - a session that ended because a token refresh failed, not because the
     * user tapped Logout (see TokenAuthenticator). */
    data object SessionExpired : LoginError() {
        override val message = "Session expired. Please sign in again."
    }

    /** Transport-level failure - see [AppError.Network]. Always retryable. */
    data class Network(val appError: AppError.Network) : LoginError() {
        override val message get() = appError.message
        override val isRetryable = true
    }

    /** A traced backend/permission failure - see [AppError.Traced]. Retryable only
     * when the failure itself suggests a transient condition (timeout, 5xx). */
    data class Traced(val appError: AppError.Traced) : LoginError() {
        override val message get() = appError.displayMessage
        override val isRetryable get() = appError.isRetryable
    }

    /** The one generic fallback in this screen. [appError]'s message is either a
     * real, specific message the backend already supplied (an unclassified business
     * error code), or the literal "Something went wrong. Try again." for a truly
     * unexpected exception - never a raw stack trace. */
    data class Unknown(val appError: AppError.Unknown = AppError.Unknown()) : LoginError() {
        override val message get() = appError.message
    }
}
