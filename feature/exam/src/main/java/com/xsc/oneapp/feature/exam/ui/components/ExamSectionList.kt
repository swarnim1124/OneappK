package com.xsc.oneapp.feature.exam.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xsc.oneapp.core.result.AppError
import com.xsc.oneapp.core.result.UiState
import com.xsc.sdk.commonui.record.EmptyState
import com.xsc.sdk.commonui.record.ErrorState
import com.xsc.sdk.commonui.record.LoadingState
import com.xsc.sdk.theme.LocalSpacing

/** Same list-of-[UiState] rendering every [com.xsc.oneapp.core.result.SectionLoader]
 * needs, moved out of each screen once the exam graph grew past one such section -
 * matches :feature:attendance's own private SectionList, kept per-module since it's a
 * thin wrapper with no cross-module contract to share. */
@Composable
fun <T> ExamSectionList(
    state: UiState<List<T>>,
    emptyMessage: String,
    onRetry: () -> Unit,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    itemContent: @Composable (T) -> Unit
) {
    val spacing = LocalSpacing.current
    when (state) {
        is UiState.Loading -> LoadingState(modifier)
        is UiState.Success -> if (state.data.isEmpty() && header == null) {
            EmptyState(emptyMessage, modifier)
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                header?.let { item { it() } }
                if (state.data.isEmpty()) {
                    item { EmptyState(emptyMessage) }
                } else {
                    items(state.data, key = key) { row -> itemContent(row) }
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry, modifier)
        is UiState.NetworkError -> ErrorState(state.message, onRetry, modifier)
        is UiState.UnexpectedError -> ErrorState(
            state.message,
            onRetry,
            modifier,
            context = (state.appError as? AppError.Traced)?.context
        )
    }
}
