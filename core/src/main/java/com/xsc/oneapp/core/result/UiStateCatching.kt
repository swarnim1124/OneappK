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
 * [label] is the human action being attempted (e.g. "attendance shortage",
 * "fee dues") - only the caller knows this, so it's threaded in rather than guessed;
 * see [SectionLoader]'s constructor for where most callers supply it. It only
 * affects the *first line* of a traced HTTP failure ("Could not load $label: [403]
 * ..."); business/network/unexpected messages don't use it since those already have
 * a specific, backend- or exception-supplied message of their own. Defaults to
 * "this request" so an unlabelled call site still reads as a sentence rather than a
 * broken template.
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
suspend fun <T> uiStateCatching(label: String = "this request", block: suspend () -> T): UiState<T> = try {
    UiState.Success(block())
} catch (e: APIError.BusinessError) {
    UiState.BusinessError(e.errorMessage)
} catch (e: APIError.NetworkError) {
    val appError = e.toAppError("Could not load $label")
    UiState.NetworkError(appError.message, appError)
} catch (e: APIError.HttpError) {
    val appError = e.toAppError("Could not load $label")
    UiState.UnexpectedError(appError.message, appError)
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    CrashReporter.recordException(e)
    UiState.UnexpectedError(e.message ?: "Something went wrong")
}
