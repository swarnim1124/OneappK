package com.xsc.oneapp.core.result

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * One independently loadable slice of a screen.
 *
 * Replaces the block every multi-section ViewModel in this app was hand-writing per
 * data set - a private MutableStateFlow, a public asStateFlow(), and a `loadX()` that
 * sets Loading then assigns `uiStateCatching { useCase() }`. AttendanceViewModel had
 * eight of those, FeeViewModel seven, TimetableViewModel eight: ~400 lines that
 * differed only in identifiers.
 *
 * The behavioural change over that pattern is [loadOnce]. Those ViewModels fired every
 * section from `init {}`, so opening one screen dispatched 7-8 concurrent requests at
 * the single `POST /` endpoint for tabs the user hadn't looked at yet - and refetched
 * all of them on every revisit. A section now loads the first time something actually
 * asks for it, and [reload] is the explicit user-driven retry/refresh.
 */
class SectionLoader<T>(
    private val scope: CoroutineScope,
    private val fetch: suspend () -> T
) {
    private val _state = MutableStateFlow<UiState<T>>(UiState.Loading)
    val state: StateFlow<UiState<T>> = _state.asStateFlow()

    private var job: Job? = null
    private var hasStarted = false

    /** Loads on first call and does nothing afterwards. Safe to call from a Composable
     * `LaunchedEffect` on every entry to a destination. */
    fun loadOnce() {
        if (hasStarted) return
        hasStarted = true
        reload()
    }

    /** Unconditional refetch, cancelling any in-flight attempt so a slow first request
     * can't land after a newer one and overwrite it. */
    fun reload() {
        hasStarted = true
        job?.cancel()
        job = scope.launch {
            _state.value = UiState.Loading
            _state.value = uiStateCatching(fetch)
        }
    }
}

/** The successful payload, or null while loading or failed. Lets a summary screen read
 * across several sections without repeating a `when` per section. */
fun <T> UiState<T>.dataOrNull(): T? = (this as? UiState.Success)?.data

/** The user-facing message for any failure state, or null when not a failure. */
fun <T> UiState<T>.errorMessageOrNull(): String? = when (this) {
    is UiState.BusinessError -> message
    is UiState.NetworkError -> message
    is UiState.UnexpectedError -> message
    else -> null
}
