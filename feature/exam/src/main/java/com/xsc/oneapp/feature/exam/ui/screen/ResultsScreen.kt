package com.xsc.oneapp.feature.exam.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.exam.domain.model.ExamResult
import com.xsc.oneapp.feature.exam.ui.components.ExamSectionList
import com.xsc.oneapp.feature.exam.ui.viewmodel.ExamViewModel
import com.xsc.sdk.commonui.record.RecordCard
import com.xsc.sdk.commonui.record.RecordRow
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.ResponsiveContent
import com.xsc.sdk.commonui.record.StatusPill
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.LocalStatusColors

/** m_exam §7.1 sm_results/result/view - one row per exam schedule: aggregate GPA/CGPA
 * only, no course-level breakdown (see [ExamResult]'s own doc comment for why). */
@Composable
fun ResultsScreen(onBack: () -> Unit, viewModel: ExamViewModel) {
    val state by viewModel.results.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadResults() }

    RecordScaffold(title = "Results", onBack = onBack) { padding ->
        Box(modifier = Modifier.fillMaxWidth().padding(padding)) {
            ExamSectionList(
                state = state,
                emptyMessage = "No results published yet.",
                onRetry = viewModel.results::reload,
                key = { it.id ?: it.hashCode() }
            ) { result ->
                ResponsiveContent {
                    ResultCard(result)
                }
            }
        }
    }
}

@Composable
private fun ResultCard(result: ExamResult) {
    val spacing = LocalSpacing.current
    val statusColors = LocalStatusColors.current
    // Nullable: a pass/fail outcome earns a scannable stripe down the card; an
    // unrecognised or still-pending status doesn't get one, only the pill still needs
    // a tint regardless so the label itself stays legible.
    val accentColor = when (result.resultStatus?.lowercase()) {
        "pass", "passed" -> statusColors.success
        "fail", "failed" -> MaterialTheme.colorScheme.error
        else -> null
    }
    val pillColor = accentColor ?: MaterialTheme.colorScheme.onSurfaceVariant

    RecordCard(accentColor = accentColor) {
        RecordRow(
            icon = Icons.Default.EmojiEvents,
            title = "Schedule ${result.scheduleId ?: "—"}",
            subtitle = result.createdAt?.let { "Published $it" },
            trailing = result.resultStatus?.let { status -> { StatusPill(status, tint = pillColor) } }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.xl)
        ) {
            GpaFigure(label = "GPA", value = result.gpa)
            GpaFigure(label = "CGPA", value = result.cgpa)
        }
    }
}

@Composable
private fun GpaFigure(label: String, value: String?) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value ?: "—",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
