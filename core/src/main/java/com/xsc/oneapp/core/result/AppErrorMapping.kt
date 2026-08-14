package com.xsc.oneapp.core.result

import com.xsc.sdk.network.APIError
import com.xsc.sdk.network.NetworkConfig
import com.xsc.sdk.network.NetworkFailureReason

/**
 * Turns a raw [APIError] into a user-facing [AppError]. [whatFailed] is the human
 * label for what was being attempted (e.g. "Could not load profile") - only the call
 * site knows this, so every caller supplies its own rather than this function
 * guessing from the dispatch tuple.
 *
 * [APIError.BusinessError] maps to [AppError.Unknown] here because a business-error
 * code is almost always feature-specific (e.g. login's own "wrong password" vs
 * "captcha rejected" vs "account locked" codes need their own classification, not a
 * generic one) - a caller that has that classification should pattern-match
 * [APIError.BusinessError] itself before ever reaching this function. [errorMessage]
 * is still real, backend-supplied copy, not a raw exception string, so wrapping it
 * as the [AppError.Unknown.raw] value is consistent with that case's own contract.
 */
fun APIError.toAppError(whatFailed: String): AppError = when (this) {
    is APIError.NetworkError -> when (reason) {
        NetworkFailureReason.NO_CONNECTION -> AppError.Network.NoConnection
        NetworkFailureReason.TIMEOUT -> AppError.Network.Timeout
        NetworkFailureReason.UNREACHABLE -> AppError.Network.Unreachable
    }

    is APIError.HttpError -> AppError.Traced(
        whatFailed = whatFailed,
        httpCode = statusCode,
        why = httpWhy(statusCode),
        context = httpContext(this)
    )

    is APIError.BusinessError -> AppError.Unknown(errorMessage.ifBlank { "Something went wrong. Try again." })
}

private fun httpWhy(statusCode: Int): String = when (statusCode) {
    403 -> "Insufficient permissions."
    404 -> "Resource not found."
    408 -> "Request timed out."
    422 -> "Validation failed."
    in 500..599 -> "Internal server error."
    else -> "Request failed."
}

/**
 * OneApp has no per-call REST path - every module shares one dispatcher endpoint
 * (see NetworkConfig/DispatcherApi), addressed by the module/submodule/action/
 * actionType tuple instead of a URL. That tuple stands in for "the endpoint" here.
 */
private fun httpContext(error: APIError.HttpError): String {
    val call = "${error.module}/${error.submodule}/${error.action}/${error.actionType}"
    return when (error.statusCode) {
        403 -> "Required: ${error.requiredPermission}"
        404 -> "Endpoint: $call"
        408 -> "Timeout after ${NetworkConfig.READ_TIMEOUT_SECONDS}s on: $call"
        422 -> "Details: ${error.errorMessage}"
        in 500..599 -> "Reference: ${error.referenceId ?: "n/a"} (share with admin)"
        else -> "Endpoint: $call"
    }
}
