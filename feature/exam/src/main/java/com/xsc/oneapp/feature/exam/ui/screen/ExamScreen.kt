@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.exam.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.exam.domain.model.ExamSchedule
import com.xsc.oneapp.feature.exam.ui.components.AdminRowActions
import com.xsc.oneapp.feature.exam.ui.components.ExamSectionList
import com.xsc.oneapp.feature.exam.ui.viewmodel.ExamAdminViewModel
import com.xsc.oneapp.feature.exam.ui.viewmodel.ExamViewModel
import com.xsc.sdk.commonui.record.RecordCard
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.ResponsiveContent
import com.xsc.sdk.commonui.record.StatusPill
import com.xsc.sdk.commonui.textfield.PremiumTextField
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.LocalStatusColors

/**
 * Schedule list plus quick links to the rest of the exam graph (results, revaluation &
 * appeals, re-exams, malpractice cases) - those four had no entry point before this
 * screen's underlying contract coverage grew past "schedules + one hall ticket". The
 * state machine, the retry path and the `onScheduleClick` contract (still gated on a
 * non-null schedule id) for the schedule list itself are unchanged.
 *
 * Status colours now resolve through LocalStatusColors so "published" and "on-hold"
 * stay legible in dark mode instead of sitting too close to the background.
 *
 * Row layout matches the design system's exam card: title/type left, status pill
 * top-right, a date row, then a bottom action row - "View Hall Ticket" when the row is
 * actually clickable, or a status-appropriate reason it isn't yet, rather than the
 * previous silent "no chevron" as the only signal a row wasn't actionable.
 */
@Composable
fun ExamScreen(
    onBack: () -> Unit,
    onScheduleClick: (scheduleId: String) -> Unit,
    onOpenResults: () -> Unit,
    onOpenRevaluation: () -> Unit,
    onOpenReExams: () -> Unit,
    onOpenMalpractice: () -> Unit,
    onOpenAdmin: () -> Unit,
    viewModel: ExamViewModel,
    adminViewModel: ExamAdminViewModel
) {
    val state by viewModel.schedules.state.collectAsStateWithLifecycle()
    val hasAnyAdminAccess by adminViewModel.hasAnyExamAdminAccess.collectAsStateWithLifecycle()
    val adminPermissions by adminViewModel.permissions.collectAsStateWithLifecycle()
    var showAddSchedule by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadSchedules() }

    RecordScaffold(
        title = "Exams",
        onBack = onBack,
        floatingActionButton = {
            if (adminPermissions.canAddExamSchedule) {
                ExtendedFloatingActionButton(
                    text = { Text("Add schedule") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = { showAddSchedule = true }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            ExamSectionList(
                state = state,
                emptyMessage = "No exam schedules published yet.",
                onRetry = viewModel.schedules::reload,
                key = { it.id ?: it.hashCode() },
                header = {
                    ResponsiveContent {
                        ExamQuickLinks(
                            onOpenResults, onOpenRevaluation, onOpenReExams, onOpenMalpractice,
                            onOpenAdmin = onOpenAdmin.takeIf { hasAnyAdminAccess }
                        )
                    }
                }
            ) { schedule ->
                ResponsiveContent {
                    ExamScheduleCard(
                        schedule,
                        onScheduleClick,
                        adminPermissions = adminPermissions,
                        onPublish = { schedule.id?.let(adminViewModel::publishExamSchedule) },
                        onHold = { schedule.id?.let(adminViewModel::holdExamSchedule) },
                        onUnpublish = { schedule.id?.let(adminViewModel::unpublishExamSchedule) },
                        onDelete = { schedule.id?.let(adminViewModel::deleteExamSchedule) }
                    )
                }
            }
        }
    }

    if (showAddSchedule) {
        AddExamScheduleDialog(
            onDismiss = { showAddSchedule = false },
            onSubmit = { name, examType, fromDate, toDate, sessionDate, session ->
                adminViewModel.addExamSchedule(name, examType, fromDate, toDate, sessionDate, session)
                showAddSchedule = false
                viewModel.schedules.reload()
            }
        )
    }
}

@Composable
private fun AddExamScheduleDialog(
    onDismiss: () -> Unit,
    onSubmit: (name: String, examType: String, fromDate: String, toDate: String, sessionDate: String, session: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var examType by remember { mutableStateOf("") }
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var sessionDate by remember { mutableStateOf("") }
    var session by remember { mutableStateOf("") }
    val spacing = LocalSpacing.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add exam schedule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                PremiumTextField(text = name, onTextChange = { name = it }, placeholder = "Exam name", imeAction = ImeAction.Next)
                PremiumTextField(text = examType, onTextChange = { examType = it }, placeholder = "Exam type (e.g. END_SEM)", imeAction = ImeAction.Next)
                PremiumTextField(text = fromDate, onTextChange = { fromDate = it }, placeholder = "From date (YYYY-MM-DD)", imeAction = ImeAction.Next)
                PremiumTextField(text = toDate, onTextChange = { toDate = it }, placeholder = "To date (YYYY-MM-DD)", imeAction = ImeAction.Next)
                PremiumTextField(text = sessionDate, onTextChange = { sessionDate = it }, placeholder = "Session date (YYYY-MM-DD)", imeAction = ImeAction.Next)
                PremiumTextField(text = session, onTextChange = { session = it }, placeholder = "Session (FN/AN)", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(name, examType, fromDate, toDate, sessionDate, session) },
                enabled = name.isNotBlank() && examType.isNotBlank() && fromDate.isNotBlank() && toDate.isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private data class ExamQuickLink(val label: String, val icon: ImageVector, val onClick: () -> Unit)

/** m_exam §7-8: results, the revaluation/appeal family, supplementary+reappear
 * registration, and malpractice cases - none of these are per-schedule, so they sit
 * above the schedule list rather than hanging off any one schedule row. The admin
 * entry point is appended only when [onOpenAdmin] is non-null - the caller passes
 * `null` for a session with no grants, so the tile itself never renders rather than
 * rendering disabled (same "show/enable only" contract every other admin gate uses). */
@Composable
private fun ExamQuickLinks(
    onOpenResults: () -> Unit,
    onOpenRevaluation: () -> Unit,
    onOpenReExams: () -> Unit,
    onOpenMalpractice: () -> Unit,
    onOpenAdmin: (() -> Unit)?
) {
    val spacing = LocalSpacing.current
    val links = buildList {
        add(ExamQuickLink("Results", Icons.AutoMirrored.Filled.Assignment, onOpenResults))
        add(ExamQuickLink("Revaluation & appeals", Icons.Default.History, onOpenRevaluation))
        add(ExamQuickLink("Re-exams", Icons.Default.Replay, onOpenReExams))
        add(ExamQuickLink("Malpractice cases", Icons.Default.Gavel, onOpenMalpractice))
        onOpenAdmin?.let { add(ExamQuickLink("Exam administration", Icons.Default.AdminPanelSettings, it)) }
    }

    Column(
        modifier = Modifier.padding(bottom = spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        links.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                row.forEach { link ->
                    RecordCard(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        onClick = link.onClick
                    ) {
                        Icon(
                            link.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            link.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = spacing.xs)
                        )
                    }
                }
                if (row.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ExamScheduleCard(
    schedule: ExamSchedule,
    onScheduleClick: (scheduleId: String) -> Unit,
    adminPermissions: com.xsc.oneapp.feature.exam.ui.state.ExamAdminPermissions,
    onPublish: () -> Unit,
    onHold: () -> Unit,
    onUnpublish: () -> Unit,
    onDelete: () -> Unit
) {
    val spacing = LocalSpacing.current
    val statusColors = LocalStatusColors.current
    val scheduleId = schedule.id
    val status = schedule.status
    val statusColor = when (status?.lowercase()) {
        "published" -> statusColors.success
        "on-hold", "onhold", "hold" -> statusColors.warning
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    // Clickable only when there is an id to open, exactly as before - but as a real M3
    // clickable surface, so it gains button semantics, a ripple and a guaranteed
    // minimum target instead of a bare Modifier.clickable.
    RecordCard(onClick = if (scheduleId != null) { { onScheduleClick(scheduleId) } } else null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    schedule.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                schedule.examType?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            if (status != null) {
                StatusPill(status, tint = statusColor)
            }
        }

        if (schedule.fromDate != null || schedule.toDate != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                modifier = Modifier.padding(top = spacing.sm)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "${schedule.fromDate ?: "?"} – ${schedule.toDate ?: "?"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacing.md),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (scheduleId != null) {
                Text(
                    "View Hall Ticket",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = spacing.xs)
                        .size(16.dp)
                )
            } else {
                // A row that isn't clickable yet now says why, rather than silently
                // omitting the chevron as the only signal - same distinction the data
                // already carried (no scheduleId means nothing to open), just labelled.
                Text(
                    when (status?.lowercase()) {
                        "on-hold", "onhold", "hold" -> "Pending review"
                        else -> "Schedule not yet published"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (scheduleId != null &&
            (adminPermissions.canPublishExamSchedule || adminPermissions.canDeleteExamSchedule)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = spacing.xs),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (adminPermissions.canPublishExamSchedule) {
                    TextButton(onClick = onPublish) { Text("Publish") }
                    TextButton(onClick = onHold) { Text("Hold") }
                    TextButton(onClick = onUnpublish) { Text("Unpublish") }
                }
                AdminRowActions(
                    canUpdate = false,
                    canDelete = adminPermissions.canDeleteExamSchedule,
                    onDelete = onDelete
                )
            }
        }
    }
}
