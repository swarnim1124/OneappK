package com.xsc.oneapp.feature.exam.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.exam.domain.model.ExamSchedule
import com.xsc.oneapp.feature.exam.ui.viewmodel.ExamViewModel
import com.xsc.sdk.commonui.record.DetailText
import com.xsc.sdk.commonui.record.EmptyState
import com.xsc.sdk.commonui.record.ErrorState
import com.xsc.sdk.commonui.record.LoadingState
import com.xsc.sdk.commonui.record.RecordCard
import com.xsc.sdk.commonui.record.RecordRow
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.ResponsiveContent
import com.xsc.sdk.commonui.record.StatusPill
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.LocalStatusColors

/**
 * Restyled only. The state machine, the retry paths and the `onScheduleClick` contract
 * (still gated on a non-null schedule id) are unchanged.
 *
 * Status colours now resolve through LocalStatusColors so "published" and "on-hold"
 * stay legible in dark mode instead of sitting too close to the background.
 */
@Composable
fun ExamScreen(
    onBack: () -> Unit,
    onScheduleClick: (scheduleId: String) -> Unit,
    viewModel: ExamViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RecordScaffold(title = "Exams", onBack = onBack) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val current = state) {
                is UiState.Loading -> LoadingState()
                is UiState.Success -> if (current.data.isEmpty()) {
                    EmptyState("No exam schedules published yet.")
                } else {
                    ExamScheduleList(current.data, onScheduleClick)
                }
                is UiState.BusinessError -> ErrorState(current.message, viewModel::load)
                is UiState.NetworkError -> ErrorState(current.message, viewModel::load)
                is UiState.UnexpectedError -> ErrorState(current.message, viewModel::load)
            }
        }
    }
}

@Composable
private fun ExamScheduleList(
    schedules: List<ExamSchedule>,
    onScheduleClick: (scheduleId: String) -> Unit
) {
    val spacing = LocalSpacing.current
    val statusColors = LocalStatusColors.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        items(schedules, key = { it.id ?: it.hashCode() }) { schedule ->
            val scheduleId = schedule.id
            val status = schedule.status
            val statusColor = when (status?.lowercase()) {
                "published" -> statusColors.success
                "on-hold", "onhold", "hold" -> statusColors.warning
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            ResponsiveContent {
                // Clickable only when there is an id to open, exactly as before - but as
                // a real M3 clickable surface, so it gains button semantics, a ripple and
                // a guaranteed minimum target instead of a bare Modifier.clickable.
                RecordCard(
                    onClick = if (scheduleId != null) {
                        { onScheduleClick(scheduleId) }
                    } else null
                ) {
                    RecordRow(
                        icon = Icons.Default.Description,
                        title = schedule.name,
                        subtitle = schedule.examType,
                        trailing = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                            ) {
                                if (status != null) {
                                    StatusPill(status, tint = statusColor)
                                }
                                if (scheduleId != null) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    )

                    if (schedule.fromDate != null || schedule.toDate != null) {
                        DetailText(
                            "${schedule.fromDate ?: "?"} – ${schedule.toDate ?: "?"}",
                            modifier = Modifier.padding(top = spacing.md)
                        )
                    }
                }
            }
        }
    }
}
