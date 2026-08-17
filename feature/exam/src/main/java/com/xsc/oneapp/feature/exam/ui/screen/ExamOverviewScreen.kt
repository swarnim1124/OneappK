package com.xsc.oneapp.feature.exam.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.exam.domain.model.ExamResult
import com.xsc.oneapp.feature.exam.domain.model.ExamSchedule
import com.xsc.oneapp.feature.exam.ui.viewmodel.ExamOverviewUiState
import com.xsc.oneapp.feature.exam.ui.viewmodel.ExamViewModel
import com.xsc.sdk.commonui.record.ErrorState
import com.xsc.sdk.commonui.record.IconBadge
import com.xsc.sdk.commonui.record.RecordCard
import com.xsc.sdk.commonui.record.RecordRow
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.ResponsiveContent
import com.xsc.sdk.commonui.record.StatusPill
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.LocalStatusColors

/**
 * The Examinations landing screen.
 *
 * The single most decision-relevant fact a student opens this module for is "how did I
 * do" once a result exists, or "when is my next exam" before one does - the headline
 * answers whichever applies without a tap. Everything else (schedules, results history,
 * revaluation & appeals) is a labelled route into its own section, following the same
 * shape as the Attendance overview.
 */
@Composable
fun ExamOverviewScreen(
    onBack: () -> Unit,
    onOpenSchedules: () -> Unit,
    onOpenResults: () -> Unit,
    onOpenRevaluation: () -> Unit,
    viewModel: ExamViewModel
) {
    val state by viewModel.overview.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadOverview() }

    RecordScaffold(title = "Examinations", onBack = onBack) { padding ->
        val fatalError = state.errorMessage
        if (fatalError != null && !state.isLoading) {
            ErrorState(
                message = fatalError,
                onRetry = viewModel::refreshOverview,
                modifier = Modifier.padding(padding)
            )
            return@RecordScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "headline") {
                ResponsiveContent { ExamHeadlineCard(state) }
            }

            item(key = "browse-heading") {
                ResponsiveContent { SectionHeading("Browse") }
            }

            item(key = "browse") {
                ResponsiveContent {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        NavigationCard(
                            icon = Icons.Default.Description,
                            title = "Schedules",
                            subtitle = pluralize(state.scheduleCount, "exam schedule", "exam schedules") { "Timetables and hall tickets" },
                            onClick = onOpenSchedules
                        )
                        NavigationCard(
                            icon = Icons.Default.WorkspacePremium,
                            title = "Results",
                            subtitle = if (state.latestResult != null) "GPA, CGPA and grade status" else "Not published yet",
                            onClick = onOpenResults
                        )
                        NavigationCard(
                            icon = Icons.Default.Gavel,
                            title = "Revaluation & appeals",
                            subtitle = pluralize(state.revaluationCount, "open request", "open requests") { "Revaluation and challenge requests" },
                            onClick = onOpenRevaluation
                        )
                    }
                }
            }
        }
    }
}

/** "N thing(s)" when the count is known and positive, otherwise [fallback] - avoids
 * printing "0 open requests" as if that were a meaningful, confirmed-zero state when
 * it may simply not have loaded yet. */
private fun pluralize(count: Int, singular: String, plural: String, fallback: () -> String): String =
    if (count > 0) "$count ${if (count == 1) singular else plural}" else fallback()

@Composable
private fun SectionHeading(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun ExamHeadlineCard(state: ExamOverviewUiState) {
    val result = state.latestResult
    val schedule = state.nextSchedule

    RecordCard {
        when {
            state.isLoading && result == null && schedule == null -> {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconBadge(Icons.Default.EventAvailable)
                    Text(
                        "Loading your examinations…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            result != null -> ResultHeadline(result)
            schedule != null -> ScheduleHeadline(schedule)
            else -> {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconBadge(Icons.Default.EventAvailable)
                    Text(
                        "No exam activity published for you yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultHeadline(result: ExamResult) {
    val spacing = LocalSpacing.current
    val statusColors = LocalStatusColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "Latest result",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
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
        StatBlock(label = "CGPA", value = result.cgpa ?: "—")
        StatBlock(label = "GPA", value = result.gpa ?: "—")
    }
}

@Composable
private fun StatBlock(label: String, value: String) {
    Column {
        Text(
            value,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ScheduleHeadline(schedule: ExamSchedule) {
    val spacing = LocalSpacing.current
    val statusColors = LocalStatusColors.current
    val status = schedule.status
    val statusColor = when (status?.lowercase()) {
        "published" -> statusColors.success
        "on-hold", "onhold", "hold" -> statusColors.warning
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "Next examination",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (status != null) {
            StatusPill(status, tint = statusColor)
        }
    }

    Text(
        schedule.name,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = spacing.md)
    )

    if (schedule.fromDate != null || schedule.toDate != null) {
        Text(
            "${schedule.fromDate ?: "?"} – ${schedule.toDate ?: "?"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = spacing.xs)
        )
    }
}

@Composable
private fun NavigationCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    RecordCard(onClick = onClick) {
        RecordRow(
            icon = icon,
            title = title,
            subtitle = subtitle,
            trailing = {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}
