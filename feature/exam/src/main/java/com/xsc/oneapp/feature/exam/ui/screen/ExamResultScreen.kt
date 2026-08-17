package com.xsc.oneapp.feature.exam.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.exam.domain.model.ExamResult
import com.xsc.oneapp.feature.exam.ui.viewmodel.ExamViewModel
import com.xsc.sdk.commonui.record.DetailText
import com.xsc.sdk.commonui.record.EmptyState
import com.xsc.sdk.commonui.record.ErrorState
import com.xsc.sdk.commonui.record.LoadingState
import com.xsc.sdk.commonui.record.RecordCard
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.ResponsiveContent
import com.xsc.sdk.commonui.record.StatusPill
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.LocalStatusColors

/**
 * One row per exam schedule (semester/exam cycle) - see [ExamResult]'s doc comment for
 * why there is no course-level breakdown here. GPA and CGPA get the deliberately large
 * treatment: they are the two values a student opens this screen to read, everything
 * else on the card is context.
 */
@Composable
fun ExamResultScreen(
    onBack: () -> Unit,
    viewModel: ExamViewModel
) {
    val state by viewModel.results.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadResults() }

    RecordScaffold(title = "Results", onBack = onBack) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val current = state) {
                is UiState.Loading -> LoadingState()
                is UiState.Success -> if (current.data.isEmpty()) {
                    EmptyState(
                        title = "No results yet",
                        message = "Results appear here once they have been calculated and published."
                    )
                } else {
                    ResultList(current.data)
                }
                is UiState.BusinessError -> ErrorState(current.message, viewModel.results::reload)
                is UiState.NetworkError -> ErrorState(current.message, viewModel.results::reload)
                is UiState.UnexpectedError -> ErrorState(current.message, viewModel.results::reload)
            }
        }
    }
}

@Composable
private fun ResultList(results: List<ExamResult>) {
    val spacing = LocalSpacing.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        items(results, key = { it.id ?: it.hashCode() }) { result ->
            ResponsiveContent { ResultCard(result) }
        }
    }
}

@Composable
private fun ResultCard(result: ExamResult) {
    val spacing = LocalSpacing.current
    val statusColors = LocalStatusColors.current

    RecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Schedule #${result.scheduleId ?: "—"}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            result.resultStatus?.let { status ->
                val tint = if (status.lowercase() == "pass") statusColors.success else MaterialTheme.colorScheme.tertiary
                StatusPill(status, tint = tint)
            }
        }

        Row(
            modifier = Modifier.padding(top = spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(spacing.xxl)
        ) {
            ResultStat(label = "CGPA", value = result.cgpa ?: "—")
            ResultStat(label = "GPA", value = result.gpa ?: "—")
        }

        result.createdAt?.let { createdAt ->
            DetailText("Calculated $createdAt", modifier = Modifier.padding(top = spacing.md))
        }
    }
}

@Composable
private fun ResultStat(label: String, value: String) {
    Column {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
