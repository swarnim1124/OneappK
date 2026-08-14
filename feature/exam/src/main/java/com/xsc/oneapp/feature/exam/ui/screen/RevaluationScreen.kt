@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.exam.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Report
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.exam.domain.model.ChallengeRevaluation
import com.xsc.oneapp.feature.exam.domain.model.ExamAppeal
import com.xsc.oneapp.feature.exam.domain.model.PhotocopyRequest
import com.xsc.oneapp.feature.exam.domain.model.RevaluationRequest
import com.xsc.oneapp.feature.exam.ui.components.ExamSectionList
import com.xsc.oneapp.feature.exam.ui.state.ExamEffect
import com.xsc.oneapp.feature.exam.ui.viewmodel.ExamAdminViewModel
import com.xsc.oneapp.feature.exam.ui.viewmodel.ExamViewModel
import com.xsc.sdk.commonui.record.RecordCard
import com.xsc.sdk.commonui.record.RecordRow
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.ResponsiveContent
import com.xsc.sdk.commonui.record.SectionChips
import com.xsc.sdk.commonui.record.StatusPill
import com.xsc.sdk.commonui.textfield.PremiumTextField
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.LocalStatusColors
import kotlinx.coroutines.launch

/**
 * The whole m_exam §8.1 sm_revaluation family in one screen, tabbed rather than four
 * separate destinations: a student thinks of these as stages of one "I want my paper
 * looked at again" story (request -> optionally get a photocopy first -> appeal the
 * outcome -> escalate to a challenge), not four unrelated features - same call already
 * made for Attendance's Exceptions/Condonations tabs.
 *
 * Challenge revaluation has no FAB of its own: the contract requires a prior regular
 * revaluation id, so it's a per-row action on a Requests card instead of a blind entry
 * point that would need the student to know an id to type in.
 */
private val TABS = listOf("Requests", "Photocopies", "Appeals", "Challenges")

@Composable
fun RevaluationScreen(onBack: () -> Unit, viewModel: ExamViewModel, adminViewModel: ExamAdminViewModel) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var showRequestDialog by remember { mutableStateOf(false) }
    var showPhotocopyDialog by remember { mutableStateOf(false) }
    var showAppealDialog by remember { mutableStateOf(false) }
    var challenging by remember { mutableStateOf<ChallengeTarget?>(null) }
    var processing by remember { mutableStateOf<String?>(null) }

    val requestsState by viewModel.revaluationRequests.state.collectAsStateWithLifecycle()
    val photocopyState by viewModel.photocopyRequests.state.collectAsStateWithLifecycle()
    val appealsState by viewModel.appeals.state.collectAsStateWithLifecycle()
    val challengesState by viewModel.challengeRevaluations.state.collectAsStateWithLifecycle()
    val adminPermissions by adminViewModel.permissions.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadRevaluation() }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is ExamEffect.ShowToast) {
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
            }
        }
    }
    LaunchedEffect(Unit) {
        adminViewModel.effect.collect { effect ->
            if (effect is ExamEffect.ShowToast) {
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
            }
        }
    }

    RecordScaffold(
        title = "Revaluation & appeals",
        onBack = onBack,
        floatingActionButton = {
            when (selected) {
                0 -> ExtendedFloatingActionButton(
                    text = { Text("New request") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = { showRequestDialog = true }
                )
                1 -> ExtendedFloatingActionButton(
                    text = { Text("Request photocopy") },
                    icon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                    onClick = { showPhotocopyDialog = true }
                )
                2 -> ExtendedFloatingActionButton(
                    text = { Text("File appeal") },
                    icon = { Icon(Icons.Default.Report, contentDescription = null) },
                    onClick = { showAppealDialog = true }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SectionChips(options = TABS, selectedIndex = selected, onSelect = { selected = it })

            Box(modifier = Modifier.fillMaxSize()) {
                when (selected) {
                    0 -> ExamSectionList(
                        state = requestsState,
                        emptyMessage = "No revaluation requests yet.",
                        onRetry = viewModel.revaluationRequests::reload,
                        key = { it.id ?: it.hashCode() }
                    ) { request ->
                        ResponsiveContent {
                            RevaluationRequestCard(
                                request,
                                onChallenge = { challenging = it },
                                canProcess = adminPermissions.canProcessRevaluation,
                                onProcess = { request.id?.let { processing = it } }
                            )
                        }
                    }
                    1 -> ExamSectionList(
                        state = photocopyState,
                        emptyMessage = "No photocopy requests yet.",
                        onRetry = viewModel.photocopyRequests::reload,
                        key = { it.id ?: it.hashCode() }
                    ) { request -> ResponsiveContent { PhotocopyRequestCard(request) } }
                    2 -> ExamSectionList(
                        state = appealsState,
                        emptyMessage = "No appeals filed yet.",
                        onRetry = viewModel.appeals::reload,
                        key = { it.id ?: it.hashCode() }
                    ) { appeal -> ResponsiveContent { AppealCard(appeal) } }
                    else -> ExamSectionList(
                        state = challengesState,
                        emptyMessage = "No challenge revaluations filed yet.",
                        onRetry = viewModel.challengeRevaluations::reload,
                        key = { it.id ?: it.hashCode() }
                    ) { challenge -> ResponsiveContent { ChallengeCard(challenge) } }
                }
            }
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.padding(padding)) { data ->
            Snackbar(snackbarData = data)
        }
    }

    if (showRequestDialog) {
        RevaluationRequestDialog(
            onDismiss = { showRequestDialog = false },
            onSubmit = { scheduleId, courseId, reason ->
                viewModel.submitRevaluationRequest(scheduleId, courseId, reason)
                showRequestDialog = false
            }
        )
    }

    if (showPhotocopyDialog) {
        PhotocopyRequestDialog(
            onDismiss = { showPhotocopyDialog = false },
            onSubmit = { scheduleId, courseId ->
                viewModel.submitPhotocopyRequest(scheduleId, courseId)
                showPhotocopyDialog = false
            }
        )
    }

    if (showAppealDialog) {
        AppealDialog(
            onDismiss = { showAppealDialog = false },
            onSubmit = { referenceId, reason ->
                viewModel.submitAppeal(referenceId, reason)
                showAppealDialog = false
            }
        )
    }

    challenging?.let { target ->
        ChallengeRevaluationDialog(
            target = target,
            onDismiss = { challenging = null },
            onSubmit = {
                viewModel.submitChallengeRevaluation(target.scheduleId, target.courseId, target.revaluationRequestId)
                challenging = null
            }
        )
    }

    processing?.let { revaluationRequestId ->
        ProcessRevaluationDialog(
            onDismiss = { processing = null },
            onSubmit = { evaluatorId ->
                adminViewModel.processRevaluation(revaluationRequestId, evaluatorId)
                processing = null
            }
        )
    }
}

/** Resolved, non-null form of the revaluation request row a "Challenge this outcome"
 * tap came from - [RevaluationRequestCard] only offers that action when all three
 * fields exist, so the dialog never has to fall back to a `!!` or a disabled confirm
 * button for a value that should already be guaranteed present. */
private data class ChallengeTarget(
    val displayId: String,
    val scheduleId: String,
    val courseId: String,
    val revaluationRequestId: String
)

/** Reads as a scannable status stripe down a card's leading edge - green for a
 * favourable/completed outcome, red for a declined/rejected one, amber while still
 * pending, no stripe when the status is unrecognised or absent (a neutral "just
 * filed" card shouldn't imply an outcome that hasn't happened yet). Mirrors the same
 * spellings already tolerated by this module's StatusPill tint logic elsewhere. */
@Composable
private fun statusAccentColor(status: String?): androidx.compose.ui.graphics.Color? {
    val statusColors = LocalStatusColors.current
    return when (status?.lowercase()) {
        "approved", "completed", "successful", "accepted", "resolved", "granted" -> statusColors.success
        "declined", "rejected", "denied" -> MaterialTheme.colorScheme.error
        "pending", "submitted", "under_review", "under review", "in_progress", "in progress" ->
            statusColors.warning
        else -> null
    }
}

@Composable
private fun RevaluationRequestCard(
    request: RevaluationRequest,
    onChallenge: (ChallengeTarget) -> Unit,
    canProcess: Boolean,
    onProcess: () -> Unit
) {
    val spacing = LocalSpacing.current
    val id = request.id
    val scheduleId = request.scheduleId
    val courseId = request.courseId

    RecordCard(accentColor = statusAccentColor(request.status)) {
        RecordRow(
            icon = Icons.Default.RateReview,
            title = "Course ${courseId ?: "—"} · Schedule ${scheduleId ?: "—"}",
            subtitle = request.reason,
            trailing = request.status?.let { status -> { StatusPill(status) } }
        )
        // Challenge revaluation requires studentId, scheduleId, courseId and a prior
        // revaluation request id (m_exam §8.1) - only offered when this row actually
        // carries all three, so ChallengeTarget never needs a nullable field.
        if (id != null && scheduleId != null && courseId != null) {
            TextButton(
                onClick = { onChallenge(ChallengeTarget(id, scheduleId, courseId, id)) },
                modifier = Modifier.padding(top = spacing.xs)
            ) {
                Icon(Icons.Default.Gavel, contentDescription = null)
                Text("Challenge this outcome", modifier = Modifier.padding(start = spacing.xs))
            }
        }
        // sm_revaluation/revaluationProcessing - assigns a re-evaluator, admin-only.
        if (canProcess && id != null) {
            TextButton(onClick = onProcess, modifier = Modifier.padding(top = spacing.xs)) {
                Text("Assign re-evaluator")
            }
        }
    }
}

@Composable
private fun PhotocopyRequestCard(request: PhotocopyRequest) {
    RecordCard(accentColor = statusAccentColor(request.status)) {
        RecordRow(
            icon = Icons.Default.ContentCopy,
            title = "Course ${request.courseId ?: "—"} · Schedule ${request.scheduleId ?: "—"}",
            subtitle = request.createdAt?.let { "Requested $it" },
            trailing = request.status?.let { status -> { StatusPill(status) } }
        )
    }
}

@Composable
private fun AppealCard(appeal: ExamAppeal) {
    RecordCard(accentColor = statusAccentColor(appeal.status)) {
        RecordRow(
            icon = Icons.Default.Report,
            title = "Appeal against #${appeal.referenceId ?: "—"}",
            subtitle = appeal.reason,
            trailing = appeal.status?.let { status -> { StatusPill(status) } }
        )
    }
}

@Composable
private fun ChallengeCard(challenge: ChallengeRevaluation) {
    RecordCard(accentColor = statusAccentColor(challenge.status)) {
        RecordRow(
            icon = Icons.Default.Gavel,
            title = "Course ${challenge.courseId ?: "—"} · Schedule ${challenge.scheduleId ?: "—"}",
            subtitle = "Challenges revaluation #${challenge.revalRequestId ?: "—"}",
            trailing = challenge.status?.let { status -> { StatusPill(status) } }
        )
    }
}

/** m_exam §8.1: revaluationRequest/add requires studentId (added by the ViewModel),
 * scheduleId and courseId. Reason is optional. */
@Composable
private fun RevaluationRequestDialog(
    onDismiss: () -> Unit,
    onSubmit: (scheduleId: String, courseId: String, reason: String?) -> Unit
) {
    val spacing = LocalSpacing.current
    var scheduleId by remember { mutableStateOf("") }
    var courseId by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Request revaluation", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PremiumTextField(
                    text = scheduleId,
                    onTextChange = { scheduleId = it },
                    placeholder = "Schedule ID",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
                PremiumTextField(
                    text = courseId,
                    onTextChange = { courseId = it },
                    placeholder = "Course ID",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
                PremiumTextField(
                    text = reason,
                    onTextChange = { reason = it },
                    placeholder = "Reason (optional)",
                    imeAction = ImeAction.Done
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(scheduleId, courseId, reason.ifBlank { null }) },
                enabled = scheduleId.isNotBlank() && courseId.isNotBlank()
            ) { Text("Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** m_exam §8.1: photocopyRequest/add requires studentId, scheduleId, courseId. */
@Composable
private fun PhotocopyRequestDialog(
    onDismiss: () -> Unit,
    onSubmit: (scheduleId: String, courseId: String) -> Unit
) {
    val spacing = LocalSpacing.current
    var scheduleId by remember { mutableStateOf("") }
    var courseId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Request photocopy", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PremiumTextField(
                    text = scheduleId,
                    onTextChange = { scheduleId = it },
                    placeholder = "Schedule ID",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
                PremiumTextField(
                    text = courseId,
                    onTextChange = { courseId = it },
                    placeholder = "Course ID",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(scheduleId, courseId) },
                enabled = scheduleId.isNotBlank() && courseId.isNotBlank()
            ) { Text("Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** m_exam §8.1: appeal/add requires studentId, referenceId and reason - referenceId is
 * whatever outcome is being appealed (typically a revaluation request id). */
@Composable
private fun AppealDialog(
    onDismiss: () -> Unit,
    onSubmit: (referenceId: String, reason: String) -> Unit
) {
    val spacing = LocalSpacing.current
    var referenceId by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("File appeal", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PremiumTextField(
                    text = referenceId,
                    onTextChange = { referenceId = it },
                    placeholder = "Revaluation request ID being appealed",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
                PremiumTextField(
                    text = reason,
                    onTextChange = { reason = it },
                    placeholder = "Reason for appeal",
                    imeAction = ImeAction.Done
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(referenceId, reason) },
                enabled = referenceId.isNotBlank() && reason.isNotBlank()
            ) { Text("Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** m_exam §8.1: challengeRevaluation/add requires a prior regular revaluation id -
 * scheduleId/courseId/revaluationRequestId are pre-filled from the row the student
 * tapped "Challenge" on rather than re-typed. */
@Composable
private fun ChallengeRevaluationDialog(
    target: ChallengeTarget,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Challenge this outcome?", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Text(
                "This escalates revaluation request #${target.displayId} (course ${target.courseId}, " +
                    "schedule ${target.scheduleId}) to a senior panel challenge revaluation.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onSubmit) { Text("Submit challenge") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** sm_revaluation/revaluationProcessing/add - assigns a re-evaluator to a filed
 * request, admin-only. */
@Composable
private fun ProcessRevaluationDialog(
    onDismiss: () -> Unit,
    onSubmit: (evaluatorId: String) -> Unit
) {
    var evaluatorId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Assign re-evaluator", style = MaterialTheme.typography.headlineSmall) },
        text = {
            PremiumTextField(
                text = evaluatorId,
                onTextChange = { evaluatorId = it },
                placeholder = "Evaluator ID",
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(evaluatorId) }, enabled = evaluatorId.isNotBlank()) { Text("Assign") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
