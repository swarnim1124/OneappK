@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.exam.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.exam.domain.model.MalpracticeCase
import com.xsc.oneapp.feature.exam.ui.components.AdminRowActions
import com.xsc.oneapp.feature.exam.ui.components.ExamSectionList
import com.xsc.oneapp.feature.exam.ui.state.ExamEffect
import com.xsc.oneapp.feature.exam.ui.viewmodel.ExamAdminViewModel
import com.xsc.oneapp.feature.exam.ui.viewmodel.ExamViewModel
import com.xsc.sdk.commonui.record.RecordCard
import com.xsc.sdk.commonui.record.RecordRow
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.ResponsiveContent
import com.xsc.sdk.commonui.record.StatusPill
import com.xsc.sdk.commonui.textfield.PremiumTextField
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.LocalStatusColors
import kotlinx.coroutines.launch

/** m_exam §8.4 sm_malpractice/malpracticeCase/view, scoped to the signed-in student for
 * the read side. Reporting an incident, recording a committee verdict and dismissing a
 * case are admin actions, gated on [ExamAdminPermissions] and folded onto this same
 * screen rather than given a dedicated destination (see the approved exam-admin plan).
 * Note this reuses the student-scoped list - an admin session sees the same rows a
 * student would (cases naming them), not an institution-wide roster; the contract has
 * no separate "all cases" view action documented, so verdict/dismiss row actions only
 * apply to whatever this list happens to show. */
@Composable
fun MalpracticeScreen(onBack: () -> Unit, viewModel: ExamViewModel, adminViewModel: ExamAdminViewModel) {
    val state by viewModel.malpracticeCases.state.collectAsStateWithLifecycle()
    val adminPermissions by adminViewModel.permissions.collectAsStateWithLifecycle()
    var showReportDialog by remember { mutableStateOf(false) }
    var recordingVerdict by remember { mutableStateOf<MalpracticeCase?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadMalpractice() }
    LaunchedEffect(Unit) {
        adminViewModel.effect.collect { effect ->
            if (effect is ExamEffect.ShowToast) {
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
            }
        }
    }

    RecordScaffold(
        title = "Malpractice cases",
        onBack = onBack,
        floatingActionButton = {
            if (adminPermissions.canReportMalpracticeCase) {
                ExtendedFloatingActionButton(
                    text = { Text("Report case") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = { showReportDialog = true }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            ExamSectionList(
                state = state,
                emptyMessage = "No malpractice cases on record.",
                onRetry = viewModel.malpracticeCases::reload,
                key = { it.id ?: it.hashCode() }
            ) { case ->
                ResponsiveContent {
                    MalpracticeCaseCard(
                        case,
                        adminPermissions = adminPermissions,
                        onRecordVerdict = { recordingVerdict = case },
                        onDismissCase = { case.id?.let(adminViewModel::dismissMalpracticeCase) }
                    )
                }
            }
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.padding(padding)) { data ->
            Snackbar(snackbarData = data)
        }
    }

    if (showReportDialog) {
        ReportMalpracticeDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = { studentId, scheduleId, description ->
                adminViewModel.reportMalpracticeCase(studentId, scheduleId, description)
                showReportDialog = false
            }
        )
    }

    recordingVerdict?.let { case ->
        RecordVerdictDialog(
            onDismiss = { recordingVerdict = null },
            onSubmit = { committee, hearingDate, verdict ->
                case.id?.let { adminViewModel.recordMalpracticeVerdict(it, committee, hearingDate, verdict) }
                recordingVerdict = null
            }
        )
    }
}

@Composable
private fun MalpracticeCaseCard(
    case: MalpracticeCase,
    adminPermissions: com.xsc.oneapp.feature.exam.ui.state.ExamAdminPermissions,
    onRecordVerdict: () -> Unit,
    onDismissCase: () -> Unit
) {
    val spacing = LocalSpacing.current
    val statusColors = LocalStatusColors.current
    // Nullable, unlike the pill's own tint below: a stripe for an unrecognised status
    // would just be a stray neutral-grey bar, whereas the pill still needs some tint
    // regardless so the status text remains legible.
    val accentColor = when (case.status?.lowercase()) {
        "resolved", "closed", "dismissed" -> statusColors.success
        "pending", "under_review", "under review" -> statusColors.warning
        else -> null
    }
    val pillColor = accentColor ?: MaterialTheme.colorScheme.onSurfaceVariant

    RecordCard(accentColor = accentColor) {
        RecordRow(
            icon = Icons.Default.Gavel,
            title = "Schedule ${case.scheduleId ?: "—"}",
            subtitle = case.description,
            trailing = case.status?.let { status -> { StatusPill(status, tint = pillColor) } }
        )
        case.hearingDate?.let {
            Text(
                "Hearing: $it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = spacing.sm)
            )
        }
        case.verdict?.let {
            Text(
                "Verdict: $it",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = spacing.xs)
            )
        }
        if (adminPermissions.canRecordMalpracticeVerdict) {
            TextButton(onClick = onRecordVerdict, modifier = Modifier.padding(top = spacing.xs)) {
                Text("Record verdict")
            }
        }
        AdminRowActions(
            canUpdate = false,
            canDelete = adminPermissions.canDismissMalpracticeCase,
            onDelete = onDismissCase
        )
    }
}

@Composable
private fun ReportMalpracticeDialog(
    onDismiss: () -> Unit,
    onSubmit: (studentId: String, scheduleId: String, description: String) -> Unit
) {
    val spacing = LocalSpacing.current
    var studentId by remember { mutableStateOf("") }
    var scheduleId by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report malpractice case") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                PremiumTextField(text = studentId, onTextChange = { studentId = it }, placeholder = "Student ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = scheduleId, onTextChange = { scheduleId = it }, placeholder = "Schedule ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = description, onTextChange = { description = it }, placeholder = "Description", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(studentId, scheduleId, description) },
                enabled = studentId.isNotBlank() && scheduleId.isNotBlank() && description.isNotBlank()
            ) { Text("Report") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun RecordVerdictDialog(
    onDismiss: () -> Unit,
    onSubmit: (committee: String?, hearingDate: String?, verdict: String) -> Unit
) {
    val spacing = LocalSpacing.current
    var committee by remember { mutableStateOf("") }
    var hearingDate by remember { mutableStateOf("") }
    var verdict by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record committee verdict") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                PremiumTextField(text = committee, onTextChange = { committee = it }, placeholder = "Committee (optional)", imeAction = ImeAction.Next)
                PremiumTextField(text = hearingDate, onTextChange = { hearingDate = it }, placeholder = "Hearing date (optional, YYYY-MM-DD)", imeAction = ImeAction.Next)
                PremiumTextField(text = verdict, onTextChange = { verdict = it }, placeholder = "Verdict", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSubmit(committee.ifBlank { null }, hearingDate.ifBlank { null }, verdict)
                },
                enabled = verdict.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
