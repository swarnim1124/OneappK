package com.xsc.sdk.network

import com.xsc.sdk.auth.requiredPermissionFor

/**
 * Typed failures thrown by [APIClient.request]. Feature ViewModels (see
 * feature/profile/.../viewmodel/ *.kt) catch these specifically to distinguish a
 * backend-declared business error from a transport failure.
 */
sealed class APIError(message: String?) : Exception(message) {

    /** `status == "error"` in a 200 envelope - a documented business validation failure. */
    data class BusinessError(val code: String, val errorMessage: String) : APIError(errorMessage)

    /** No connectivity, timeout, DNS failure, etc. - the request never reached/returned
     * from the server. [reason] is derived from the underlying [java.io.IOException]
     * subtype at the throw site (see [APIClient.request]) so a caller can tell "no
     * internet" apart from "reached the network but the host never answered" without
     * re-parsing [errorMessage]. */
    data class NetworkError(
        val errorMessage: String,
        val reason: NetworkFailureReason = NetworkFailureReason.UNREACHABLE
    ) : APIError(errorMessage)

    /**
     * Non-2xx HTTP status the envelope itself doesn't explain (422/500/405 per the
     * contracts). Carries the full dispatch call context - not just the status code -
     * so a failure is traceable back to exactly which permission or call it was
     * without attaching a debugger: [module]/[submodule]/[action]/[actionType] are the
     * same four fields passed to [APIClient.request], and [requiredPermission] is
     * that same tuple already rendered as the permission string the backend's
     * authorization check would have matched against (see
     * [com.xsc.sdk.auth.requiredPermissionFor]). [referenceId] is only populated for
     * 5xx - a client-generated id to hand to an admin/support, since a 5xx by
     * definition means the server-side cause isn't something the client can name.
     */
    data class HttpError(
        val statusCode: Int,
        val errorMessage: String,
        // Defaulted (not required at every call site) so existing callers that only
        // ever cared about the status code - mostly tests simulating "some HTTP
        // failure happened" - don't need updating just to keep compiling. A real
        // caller that wants tracing (see APIClient.request, LoginRepositoryImpl)
        // passes its own values.
        val module: String = "unknown",
        val submodule: String = "unknown",
        val action: String = "unknown",
        val actionType: String = "unknown",
        val requiredPermission: String = requiredPermissionFor(module, submodule, action, actionType),
        val referenceId: String? = null
    ) : APIError(errorMessage)
}

/** See [APIError.NetworkError.reason]. */
enum class NetworkFailureReason { NO_CONNECTION, UNREACHABLE, TIMEOUT }
