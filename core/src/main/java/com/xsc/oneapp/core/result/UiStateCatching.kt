package com.xsc.oneapp.core.result

import com.xsc.oneapp.core.observability.CrashReporter
import com.xsc.sdk.network.APIError
import kotlinx.coroutines.CancellationException

/**
 * Runs [block] and maps its outcome to a [UiState], catching the same APIError
 * taxonomy every feature ViewModel currently handles by hand (see LoginViewModel,
 * PersonalDetailViewModel, ExamViewModel, AttendanceViewModel, CurriculumViewModel).
 * One place to keep that mapping consistent as more ViewModels migrate onto it.
 *
 * This is also the one place every *unclassified* failure across the app already
 * funnels through, which makes it the natural spot to report those to Crashlytics
 * (see CrashReporter) - the alternative was every ViewModel's catch block deciding
 * for itself, or nobody reporting anything (the previous state: zero crash
 * visibility, see PRODUCTION_READINESS_AUDIT.md H-8). [APIError.BusinessError] and
 * [APIError.NetworkError] are expected, handled failure modes (the user sees a
 * retry/error state either way) and are deliberately not reported here - only the
 * `Exception` branch, which by definition wasn't anticipated by anything upstream.
 */
suspend fun <T> uiStateCatching(block: suspend () -> T): UiState<T> = try {
    UiState.Success(block())
} catch (e: APIError.BusinessError) {
    UiState.BusinessError(e.errorMessage)
} catch (e: APIError.NetworkError) {
    UiState.NetworkError(e.errorMessage)
} catch (e: APIError.HttpError) {
    UiState.UnexpectedError("Server error (${e.statusCode})")
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    CrashReporter.recordException(e)
    UiState.UnexpectedError(e.message ?: "Something went wrong")
}
