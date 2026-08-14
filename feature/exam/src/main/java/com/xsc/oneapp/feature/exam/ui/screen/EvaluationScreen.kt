@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.exam.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
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
import com.xsc.oneapp.feature.exam.domain.model.EvaluationBundle
import com.xsc.oneapp.feature.exam.ui.components.AdminRowActions
import com.xsc.oneapp.feature.exam.ui.components.ExamSectionList
import com.xsc.oneapp.feature.exam.ui.state.ExamEffect
import com.xsc.oneapp.feature.exam.ui.viewmodel.ExamAdminViewModel
import com.xsc.sdk.commonui.button.PrimaryButton
import com.xsc.sdk.commonui.record.RecordCard
import com.xsc.sdk.commonui.record.RecordRow
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.ResponsiveContent
import com.xsc.sdk.commonui.record.SectionChips
import com.xsc.sdk.commonui.record.StatusPill
import com.xsc.sdk.commonui.textfield.PremiumTextField
import com.xsc.sdk.theme.LocalSpacing
import kotlinx.coroutines.launch

/** m_exam §6.3 sm_evaluation: answer-script valuation bundles, second valuation
 * requests and moderation, tabbed as the three admin actions in this submodule -
 * Second Valuation and Moderation have no view action of their own in the contract, so
 * both are a bare gated action form rather than an [ExamSectionList]. */
private val TABS = listOf("Bundles", "Second Valuation", "Moderation")

@Composable
fun EvaluationScreen(onBack: () -> Unit, viewModel: ExamAdminViewModel) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var showAddBundle by remember { mutableStateOf(false) }
    var editingBundle by remember { mutableStateOf<EvaluationBundle?>(null) }

    val bundlesState by viewModel.evaluationBundles.state.collectAsStateWithLifecycle()
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadEvaluationAdmin() }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is ExamEffect.ShowToast) {
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
            }
        }
    }

    RecordScaffold(
        title = "Evaluation",
        onBack = onBack,
        floatingActionButton = {
            if (selected == 0 && permissions.canAddEvaluationBundle) {
                ExtendedFloatingActionButton(
                    text = { Text("Assign bundle") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = { showAddBundle = true }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SectionChips(options = TABS, selectedIndex = selected, onSelect = { selected = it })

            Box(modifier = Modifier.fillMaxSize()) {
                when (selected) {
                    0 -> ExamSectionList(
                        state = bundlesState,
                        emptyMessage = "No evaluation bundles assigned yet.",
                        onRetry = viewModel.evaluationBundles::reload,
                        key = { it.id ?: it.hashCode() }
                    ) { bundle ->
                        ResponsiveContent {
                            EvaluationBundleCard(
                                bundle,
                                canUpdate = permissions.canUpdateEvaluationBundle,
                                canDelete = permissions.canDeleteEvaluationBundle,
                                onUpdate = { editingBundle = bundle },
                                onDelete = { bundle.id?.let(viewModel::deleteEvaluationBundle) }
                            )
                        }
                    }
                    1 -> SecondValuationTab(
                        canRequest = permissions.canRequestSecondValuation,
                        onRequest = viewModel::requestSecondValuation
                    )
                    else -> ModerationTab(
                        canApply = permissions.canApplyModeration,
                        onApply = viewModel::applyModeration
                    )
                }
            }
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.padding(padding)) { data ->
            Snackbar(snackbarData = data)
        }
    }

    if (showAddBundle) {
        AddEvaluationBundleDialog(
            onDismiss = { showAddBundle = false },
            onSubmit = { scheduleId, courseId, evaluatorId, studentIds ->
                viewModel.addEvaluationBundle(scheduleId, courseId, evaluatorId, studentIds)
                showAddBundle = false
            }
        )
    }

    editingBundle?.let { bundle ->
        UpdateEvaluationBundleDialog(
            bundle = bundle,
            onDismiss = { editingBundle = null },
            onSubmit = { status ->
                bundle.id?.let { viewModel.updateEvaluationBundle(it, status) }
                editingBundle = null
            }
        )
    }
}

@Composable
private fun EvaluationBundleCard(
    bundle: EvaluationBundle,
    canUpdate: Boolean,
    canDelete: Boolean,
    onUpdate: () -> Unit,
    onDelete: () -> Unit
) {
    RecordCard {
        RecordRow(
            icon = Icons.Default.Groups,
            title = "Evaluator ${bundle.evaluatorId ?: "—"}",
            subtitle = "Schedule ${bundle.scheduleId ?: "—"} · Course ${bundle.courseId ?: "—"} · " +
                "${bundle.studentIds.size} student(s): ${bundle.studentIds.joinToString()}",
            trailing = bundle.status?.let { status -> { StatusPill(status) } }
        )
        AdminRowActions(
            canUpdate = canUpdate,
            canDelete = canDelete,
            onUpdate = onUpdate,
            onDelete = onDelete
        )
    }
}

@Composable
private fun SecondValuationTab(
    canRequest: Boolean,
    onRequest: (scheduleId: String, courseId: String, studentId: String) -> Unit
) {
    val spacing = LocalSpacing.current
    var scheduleId by remember { mutableStateOf("") }
    var courseId by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        ResponsiveContent {
            Column(
                modifier = Modifier.padding(spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                Text(
                    "Request a second valuation for a student's answer script.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PremiumTextField(text = scheduleId, onTextChange = { scheduleId = it }, placeholder = "Schedule ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = courseId, onTextChange = { courseId = it }, placeholder = "Course ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = studentId, onTextChange = { studentId = it }, placeholder = "Student ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                if (canRequest) {
                    PrimaryButton(
                        text = "Request",
                        onClick = { onRequest(scheduleId, courseId, studentId) },
                        enabled = scheduleId.isNotBlank() && courseId.isNotBlank() && studentId.isNotBlank()
                    )
                } else {
                    Text(
                        "You don't have permission to request a second valuation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ModerationTab(canApply: Boolean, onApply: (scheduleId: String, courseId: String, adjustment: String) -> Unit) {
    val spacing = LocalSpacing.current
    var scheduleId by remember { mutableStateOf("") }
    var courseId by remember { mutableStateOf("") }
    var adjustment by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        ResponsiveContent {
            Column(
                modifier = Modifier.padding(spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                Text(
                    "Apply a moderation adjustment to a schedule and course's marks.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PremiumTextField(text = scheduleId, onTextChange = { scheduleId = it }, placeholder = "Schedule ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = courseId, onTextChange = { courseId = it }, placeholder = "Course ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = adjustment, onTextChange = { adjustment = it }, placeholder = "Adjustment (e.g. +2)", imeAction = ImeAction.Done)
                if (canApply) {
                    PrimaryButton(
                        text = "Apply",
                        onClick = { onApply(scheduleId, courseId, adjustment) },
                        enabled = scheduleId.isNotBlank() && courseId.isNotBlank() && adjustment.isNotBlank()
                    )
                } else {
                    Text(
                        "You don't have permission to apply moderation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** m_exam §6.3: evaluationBundle/add requires scheduleId, courseId, evaluatorId and a
 * non-empty studentIds[] - captured here as a comma-separated field since a bundle is
 * inherently a batch of students, unlike the single-pair marks-entry dialogs
 * elsewhere in this admin surface. */
@Composable
private fun AddEvaluationBundleDialog(
    onDismiss: () -> Unit,
    onSubmit: (scheduleId: String, courseId: String, evaluatorId: String, studentIds: List<String>) -> Unit
) {
    val spacing = LocalSpacing.current
    var scheduleId by remember { mutableStateOf("") }
    var courseId by remember { mutableStateOf("") }
    var evaluatorId by remember { mutableStateOf("") }
    var studentIdsText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Assign evaluation bundle", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                PremiumTextField(text = scheduleId, onTextChange = { scheduleId = it }, placeholder = "Schedule ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = courseId, onTextChange = { courseId = it }, placeholder = "Course ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = evaluatorId, onTextChange = { evaluatorId = it }, placeholder = "Evaluator ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = studentIdsText, onTextChange = { studentIdsText = it }, placeholder = "Student IDs, comma-separated", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            val studentIds = studentIdsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            TextButton(
                onClick = { onSubmit(scheduleId, courseId, evaluatorId, studentIds) },
                enabled = scheduleId.isNotBlank() && courseId.isNotBlank() && evaluatorId.isNotBlank() && studentIds.isNotEmpty()
            ) { Text("Assign") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun UpdateEvaluationBundleDialog(
    bundle: EvaluationBundle,
    onDismiss: () -> Unit,
    onSubmit: (status: String?) -> Unit
) {
    var status by remember { mutableStateOf(bundle.status.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Update evaluation bundle", style = MaterialTheme.typography.headlineSmall) },
        text = {
            PremiumTextField(text = status, onTextChange = { status = it }, placeholder = "Status", imeAction = ImeAction.Done)
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(status.ifBlank { null }) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
