package com.xsc.oneapp.core.result

/**
 * Shared shape for screen-level state, matching what login/profile/exam/attendance/
 * curriculum each hand-roll today as their own per-feature sealed class (LoginState,
 * PersonalDetailState, ExamScheduleState, ...). Not wired into any ViewModel yet -
 * that migration happens feature by feature; this just gives it a single home.
 */
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class BusinessError(val message: String) : UiState<Nothing>()
    data class NetworkError(val message: String) : UiState<Nothing>()
    data class UnexpectedError(val message: String) : UiState<Nothing>()
}
