package com.xsc.oneapp.core.result

/**
 * Shared shape for screen-level state - the backbone of most feature ViewModels via
 * [SectionLoader]/[uiStateCatching] (Attendance, Fee, Curriculum, Timetable, Exam).
 * Profile and Login hand-roll their own per-screen sealed classes instead (different
 * enough shapes - a multi-field form vs. a single list - that forcing them through
 * this one wouldn't simplify anything), so don't assume every screen uses this.
 *
 * [appError], on the three failure cases, is optional and defaults to null: it's
 * populated by [uiStateCatching] for a [com.xsc.sdk.network.APIError.NetworkError] or
 * [com.xsc.sdk.network.APIError.HttpError] specifically, carrying the full
 * [AppError.Network]/[AppError.Traced] object for a caller that wants the richer
 * two-line traced display and a Retry affordance (see
 * [com.xsc.sdk.commonui.error.TracedErrorCard]) rather than just [message]. A caller
 * that only ever displayed [message] before this field existed keeps working
 * unchanged - [message] is still populated from the same [AppError], just flattened
 * to one line.
 */
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class BusinessError(val message: String, val appError: AppError? = null) : UiState<Nothing>()
    data class NetworkError(val message: String, val appError: AppError? = null) : UiState<Nothing>()
    data class UnexpectedError(val message: String, val appError: AppError? = null) : UiState<Nothing>()
}

/**
 * True only once this has definitively resolved to [UiState.Success] and [route]
 * isn't in it. [UiState.Loading] and every failure variant return false on purpose -
 * a route guard built on this must never bounce a user out of a screen they were
 * already let into just because a fetch hasn't resolved yet or failed transiently.
 * Shared by :app's RootNavHost and feature-owned nav graphs (AttendanceNavigation,
 * ProfileNavigation) so the same fail-open-while-unresolved rule can't drift between
 * them - kept as a plain function here rather than a Composable so :core doesn't
 * need a Compose dependency just to host it.
 */
fun UiState<Set<String>>.deniesRoute(route: String): Boolean =
    this is UiState.Success && route !in data
