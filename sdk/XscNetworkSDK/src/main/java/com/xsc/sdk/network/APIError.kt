package com.xsc.sdk.network

/**
 * Typed failures thrown by [APIClient.request]. Feature ViewModels (see
 * feature/profile/.../viewmodel/ *.kt) catch these specifically to distinguish a
 * backend-declared business error from a transport failure.
 */
sealed class APIError(message: String?) : Exception(message) {

    /** `status == "error"` in a 200 envelope - a documented business validation failure. */
    data class BusinessError(val code: String, val errorMessage: String) : APIError(errorMessage)

    /** No connectivity, timeout, DNS failure, etc. - the request never reached/returned from the server. */
    data class NetworkError(val errorMessage: String) : APIError(errorMessage)

    /** Non-2xx HTTP status the envelope itself doesn't explain (422/500/405 per the contracts). */
    data class HttpError(val statusCode: Int, val errorMessage: String) : APIError(errorMessage)
}
