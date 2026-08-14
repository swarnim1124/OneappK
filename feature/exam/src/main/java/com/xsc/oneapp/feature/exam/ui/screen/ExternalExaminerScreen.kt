@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.exam.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.exam.domain.model.ExternalEvaluation
import com.xsc.oneapp.feature.exam.domain.model.ExternalExaminer
import com.xsc.oneapp.feature.exam.domain.model.ExternalPaperSetting
import com.xsc.oneapp.feature.exam.ui.components.AdminRowActions
import com.xsc.oneapp.feature.exam.ui.components.ExamSectionList
import com.xsc.oneapp.feature.exam.ui.state.ExamEffect
import com.xsc.oneapp.feature.exam.ui.viewmodel.ExamAdminViewModel
import com.xsc.sdk.commonui.record.RecordCard
import com.xsc.sdk.commonui.record.RecordRow
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.ResponsiveContent
import com.xsc.sdk.commonui.record.SectionChips
import com.xsc.sdk.commonui.record.StatusPill
import com.xsc.sdk.commonui.textfield.PremiumTextField
import com.xsc.sdk.theme.LocalSpacing
import kotlinx.coroutines.launch

private val TAB_TITLES = listOf("Examiners", "Paper Settings", "Evaluations")

/** m_exam §5.2 sm_externalExam. */
@Composable
fun ExternalExaminerScreen(onBack: () -> Unit, viewModel: ExamAdminViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val examinersState by viewModel.externalExaminers.state.collectAsStateWithLifecycle()
    val paperSettingsState by viewModel.externalPaperSettings.state.collectAsStateWithLifecycle()
    val evaluationsState by viewModel.externalEvaluations.state.collectAsStateWithLifecycle()
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()

    var showAddExaminer by remember { mutableStateOf(false) }
    var editingExaminer by remember { mutableStateOf<ExternalExaminer?>(null) }
    var showAddPaperSetting by remember { mutableStateOf(false) }
    var showAddEvaluation by remember { mutableStateOf(false) }
    var editingEvaluation by remember { mutableStateOf<ExternalEvaluation?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadExternalExamAdmin() }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is ExamEffect.ShowToast) {
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
            }
        }
    }

    RecordScaffold(
        title = "External examiners",
        onBack = onBack,
        floatingActionButton = {
            when (selectedTab) {
                0 -> if (permissions.canAddExternalExaminer) {
                    ExtendedFloatingActionButton(
                        text = { Text("Register examiner") },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = { showAddExaminer = true }
                    )
                }
                1 -> if (permissions.canAddExternalPaperSetting) {
                    ExtendedFloatingActionButton(
                        text = { Text("Assign") },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = { showAddPaperSetting = true }
                    )
                }
                2 -> if (permissions.canAddExternalEvaluation) {
                    ExtendedFloatingActionButton(
                        text = { Text("Dispatch evaluation") },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = { showAddEvaluation = true }
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SectionChips(
                options = TAB_TITLES,
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it }
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> ExaminersTab(
                        state = examinersState,
                        onRetry = viewModel.externalExaminers::reload,
                        canUpdate = permissions.canUpdateExternalExaminer,
                        canDelete = permissions.canDeleteExternalExaminer,
                        onEdit = { editingExaminer = it },
                        onDelete = { it.id?.let(viewModel::deleteExternalExaminer) }
                    )
                    1 -> PaperSettingsTab(
                        state = paperSettingsState,
                        onRetry = viewModel.externalPaperSettings::reload,
                        canDelete = permissions.canDeleteExternalPaperSetting,
                        onDelete = { it.id?.let(viewModel::deleteExternalPaperSetting) }
                    )
                    2 -> EvaluationsTab(
                        state = evaluationsState,
                        onRetry = viewModel.externalEvaluations::reload,
                        canUpdate = permissions.canUpdateExternalEvaluation,
                        canDelete = permissions.canDeleteExternalEvaluation,
                        onEdit = { editingEvaluation = it },
                        onDelete = { it.id?.let(viewModel::deleteExternalEvaluation) }
                    )
                }
            }
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.padding(padding)) { data ->
            Snackbar(snackbarData = data)
        }
    }

    if (showAddExaminer) {
        AddExternalExaminerDialog(
            onDismiss = { showAddExaminer = false },
            onSubmit = { examinerName, contactEmail, contactPhone, specialization ->
                viewModel.addExternalExaminer(examinerName, contactEmail, contactPhone, specialization)
                showAddExaminer = false
            }
        )
    }

    editingExaminer?.let { examiner ->
        EditExternalExaminerDialog(
            examiner = examiner,
            onDismiss = { editingExaminer = null },
            onSubmit = { examinerName, contactEmail, contactPhone, specialization ->
                examiner.id?.let {
                    viewModel.updateExternalExaminer(it, examinerName, contactEmail, contactPhone, specialization)
                }
                editingExaminer = null
            }
        )
    }

    if (showAddPaperSetting) {
        AddExternalPaperSettingDialog(
            onDismiss = { showAddPaperSetting = false },
            onSubmit = { examinerId, courseId, scheduleId ->
                viewModel.addExternalPaperSetting(examinerId, courseId, scheduleId)
                showAddPaperSetting = false
            }
        )
    }

    if (showAddEvaluation) {
        AddExternalEvaluationDialog(
            onDismiss = { showAddEvaluation = false },
            onSubmit = { examinerId, courseId, scheduleId, scriptBundleIds ->
                viewModel.addExternalEvaluation(examinerId, courseId, scheduleId, scriptBundleIds)
                showAddEvaluation = false
            }
        )
    }

    editingEvaluation?.let { evaluation ->
        EditExternalEvaluationDialog(
            evaluation = evaluation,
            onDismiss = { editingEvaluation = null },
            onSubmit = { status ->
                evaluation.id?.let { viewModel.updateExternalEvaluation(it, status) }
                editingEvaluation = null
            }
        )
    }
}

// --- Examiners ---

@Composable
private fun ExaminersTab(
    state: UiState<List<ExternalExaminer>>,
    onRetry: () -> Unit,
    canUpdate: Boolean,
    canDelete: Boolean,
    onEdit: (ExternalExaminer) -> Unit,
    onDelete: (ExternalExaminer) -> Unit
) {
    val spacing = LocalSpacing.current
    ExamSectionList(
        state = state,
        emptyMessage = "No external examiners registered yet.",
        onRetry = onRetry,
        key = { it.id ?: it.hashCode() }
    ) { examiner ->
        ResponsiveContent {
            RecordCard {
                RecordRow(
                    icon = Icons.Default.Person,
                    title = examiner.examinerName ?: "—",
                    subtitle = examiner.specialization
                )
                if (examiner.contactEmail != null || examiner.contactPhone != null) {
                    Text(
                        listOfNotNull(examiner.contactEmail, examiner.contactPhone).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = spacing.sm)
                    )
                }
                AdminRowActions(
                    canUpdate = canUpdate,
                    canDelete = canDelete,
                    onUpdate = { onEdit(examiner) },
                    onDelete = { onDelete(examiner) }
                )
            }
        }
    }
}

// --- Paper Settings ---

@Composable
private fun PaperSettingsTab(
    state: UiState<List<ExternalPaperSetting>>,
    onRetry: () -> Unit,
    canDelete: Boolean,
    onDelete: (ExternalPaperSetting) -> Unit
) {
    ExamSectionList(
        state = state,
        emptyMessage = "No paper-setting assignments yet.",
        onRetry = onRetry,
        key = { it.id ?: it.hashCode() }
    ) { setting ->
        ResponsiveContent {
            RecordCard {
                RecordRow(
                    icon = Icons.Default.Assignment,
                    title = "Course ${setting.courseId ?: "—"}",
                    subtitle = "Schedule ${setting.scheduleId ?: "—"} · Examiner ${setting.examinerId ?: "—"}"
                )
                AdminRowActions(
                    canUpdate = false,
                    canDelete = canDelete,
                    onDelete = { onDelete(setting) }
                )
            }
        }
    }
}

// --- Evaluations ---

@Composable
private fun EvaluationsTab(
    state: UiState<List<ExternalEvaluation>>,
    onRetry: () -> Unit,
    canUpdate: Boolean,
    canDelete: Boolean,
    onEdit: (ExternalEvaluation) -> Unit,
    onDelete: (ExternalEvaluation) -> Unit
) {
    val spacing = LocalSpacing.current
    ExamSectionList(
        state = state,
        emptyMessage = "No evaluations dispatched yet.",
        onRetry = onRetry,
        key = { it.id ?: it.hashCode() }
    ) { evaluation ->
        ResponsiveContent {
            RecordCard {
                RecordRow(
                    icon = Icons.Default.RateReview,
                    title = "Course ${evaluation.courseId ?: "—"}",
                    subtitle = "Schedule ${evaluation.scheduleId ?: "—"} · Examiner ${evaluation.examinerId ?: "—"}",
                    trailing = evaluation.status?.let { status -> { StatusPill(status) } }
                )
                if (evaluation.scriptBundleIds.isNotEmpty()) {
                    Text(
                        "Bundles: ${evaluation.scriptBundleIds.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = spacing.sm)
                    )
                }
                AdminRowActions(
                    canUpdate = canUpdate,
                    canDelete = canDelete,
                    onUpdate = { onEdit(evaluation) },
                    onDelete = { onDelete(evaluation) }
                )
            }
        }
    }
}

// --- Dialogs: External Examiner ---

@Composable
private fun AddExternalExaminerDialog(
    onDismiss: () -> Unit,
    onSubmit: (examinerName: String, contactEmail: String?, contactPhone: String?, specialization: String?) -> Unit
) {
    val spacing = LocalSpacing.current
    var examinerName by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Register examiner", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PremiumTextField(text = examinerName, onTextChange = { examinerName = it }, placeholder = "Examiner name", imeAction = ImeAction.Next)
                PremiumTextField(text = contactEmail, onTextChange = { contactEmail = it }, placeholder = "Contact email (optional)", keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                PremiumTextField(text = contactPhone, onTextChange = { contactPhone = it }, placeholder = "Contact phone (optional)", keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
                PremiumTextField(text = specialization, onTextChange = { specialization = it }, placeholder = "Specialization (optional)", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(
                enabled = examinerName.isNotBlank(),
                onClick = {
                    onSubmit(
                        examinerName,
                        contactEmail.ifBlank { null },
                        contactPhone.ifBlank { null },
                        specialization.ifBlank { null }
                    )
                }
            ) { Text("Register") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EditExternalExaminerDialog(
    examiner: ExternalExaminer,
    onDismiss: () -> Unit,
    onSubmit: (examinerName: String?, contactEmail: String?, contactPhone: String?, specialization: String?) -> Unit
) {
    val spacing = LocalSpacing.current
    var examinerName by remember { mutableStateOf(examiner.examinerName.orEmpty()) }
    var contactEmail by remember { mutableStateOf(examiner.contactEmail.orEmpty()) }
    var contactPhone by remember { mutableStateOf(examiner.contactPhone.orEmpty()) }
    var specialization by remember { mutableStateOf(examiner.specialization.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Edit examiner", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PremiumTextField(text = examinerName, onTextChange = { examinerName = it }, placeholder = "Examiner name", imeAction = ImeAction.Next)
                PremiumTextField(text = contactEmail, onTextChange = { contactEmail = it }, placeholder = "Contact email", keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                PremiumTextField(text = contactPhone, onTextChange = { contactPhone = it }, placeholder = "Contact phone", keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
                PremiumTextField(text = specialization, onTextChange = { specialization = it }, placeholder = "Specialization", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSubmit(
                        examinerName.ifBlank { null },
                        contactEmail.ifBlank { null },
                        contactPhone.ifBlank { null },
                        specialization.ifBlank { null }
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// --- Dialog: External Paper Setting ---

@Composable
private fun AddExternalPaperSettingDialog(
    onDismiss: () -> Unit,
    onSubmit: (examinerId: String, courseId: String, scheduleId: String) -> Unit
) {
    val spacing = LocalSpacing.current
    var examinerId by remember { mutableStateOf("") }
    var courseId by remember { mutableStateOf("") }
    var scheduleId by remember { mutableStateOf("") }

    val canSubmit = examinerId.isNotBlank() && courseId.isNotBlank() && scheduleId.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Assign paper setting", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PremiumTextField(text = examinerId, onTextChange = { examinerId = it }, placeholder = "Examiner ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = courseId, onTextChange = { courseId = it }, placeholder = "Course ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = scheduleId, onTextChange = { scheduleId = it }, placeholder = "Schedule ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(enabled = canSubmit, onClick = { onSubmit(examinerId, courseId, scheduleId) }) { Text("Assign") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// --- Dialogs: External Evaluation ---

@Composable
private fun AddExternalEvaluationDialog(
    onDismiss: () -> Unit,
    onSubmit: (examinerId: String, courseId: String, scheduleId: String, scriptBundleIds: List<String>) -> Unit
) {
    val spacing = LocalSpacing.current
    var examinerId by remember { mutableStateOf("") }
    var courseId by remember { mutableStateOf("") }
    var scheduleId by remember { mutableStateOf("") }
    var scriptBundleIdsText by remember { mutableStateOf("") }

    val scriptBundleIds = scriptBundleIdsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val canSubmit = examinerId.isNotBlank() && courseId.isNotBlank() && scheduleId.isNotBlank() && scriptBundleIds.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Dispatch evaluation", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PremiumTextField(text = examinerId, onTextChange = { examinerId = it }, placeholder = "Examiner ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = courseId, onTextChange = { courseId = it }, placeholder = "Course ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = scheduleId, onTextChange = { scheduleId = it }, placeholder = "Schedule ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = scriptBundleIdsText, onTextChange = { scriptBundleIdsText = it }, placeholder = "Script bundle ID(s), comma-separated", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSubmit,
                onClick = { onSubmit(examinerId, courseId, scheduleId, scriptBundleIds) }
            ) { Text("Dispatch") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EditExternalEvaluationDialog(
    evaluation: ExternalEvaluation,
    onDismiss: () -> Unit,
    onSubmit: (status: String?) -> Unit
) {
    val spacing = LocalSpacing.current
    var status by remember { mutableStateOf(evaluation.status.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Update evaluation", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PremiumTextField(text = status, onTextChange = { status = it }, placeholder = "Status", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(status.ifBlank { null }) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
