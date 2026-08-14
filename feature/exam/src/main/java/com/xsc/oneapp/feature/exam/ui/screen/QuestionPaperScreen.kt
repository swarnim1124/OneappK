@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.exam.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Send
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
import com.xsc.oneapp.feature.exam.domain.model.QuestionBankEntry
import com.xsc.oneapp.feature.exam.domain.model.QuestionPaper
import com.xsc.oneapp.feature.exam.domain.model.QuestionPaperSubmission
import com.xsc.oneapp.feature.exam.ui.components.AdminRowActions
import com.xsc.oneapp.feature.exam.ui.components.ExamSectionList
import com.xsc.oneapp.feature.exam.ui.state.ExamAdminPermissions
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

private val TAB_TITLES = listOf("Papers", "Submissions", "Approval", "Bank")

/** m_exam §5.1 sm_questionPaper. "Approval" has no dedicated view action in the
 * contract - it acts on the same [ExamAdminViewModel.questionPapers] list the
 * "Papers" tab shows, just surfacing the approve/reject actions instead of edit/delete. */
@Composable
fun QuestionPaperScreen(onBack: () -> Unit, viewModel: ExamAdminViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val papersState by viewModel.questionPapers.state.collectAsStateWithLifecycle()
    val submissionsState by viewModel.questionPaperSubmissions.state.collectAsStateWithLifecycle()
    val bankState by viewModel.questionBank.state.collectAsStateWithLifecycle()
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()

    var showAddPaper by remember { mutableStateOf(false) }
    var editingPaper by remember { mutableStateOf<QuestionPaper?>(null) }
    var rejectingPaper by remember { mutableStateOf<QuestionPaper?>(null) }
    var showSubmitPaper by remember { mutableStateOf(false) }
    var showAddBankEntry by remember { mutableStateOf(false) }
    var editingBankEntry by remember { mutableStateOf<QuestionBankEntry?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadQuestionPaperAdmin() }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is ExamEffect.ShowToast) {
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
            }
        }
    }

    RecordScaffold(
        title = "Question papers",
        onBack = onBack,
        floatingActionButton = {
            when (selectedTab) {
                0 -> if (permissions.canAddQuestionPaper) {
                    ExtendedFloatingActionButton(
                        text = { Text("Draft paper") },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = { showAddPaper = true }
                    )
                }
                1 -> if (permissions.canSubmitQuestionPaper) {
                    ExtendedFloatingActionButton(
                        text = { Text("Submit for review") },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = { showSubmitPaper = true }
                    )
                }
                3 -> if (permissions.canAddQuestionBankEntry) {
                    ExtendedFloatingActionButton(
                        text = { Text("Add entry") },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = { showAddBankEntry = true }
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
                    0 -> PapersTab(
                        state = papersState,
                        onRetry = viewModel.questionPapers::reload,
                        permissions = permissions,
                        onEdit = { editingPaper = it },
                        onDelete = { it.id?.let(viewModel::deleteQuestionPaper) },
                        showApprovalActions = false,
                        onApprove = {},
                        onReject = {}
                    )
                    1 -> SubmissionsTab(
                        state = submissionsState,
                        onRetry = viewModel.questionPaperSubmissions::reload,
                        canDelete = permissions.canDeleteQuestionPaperSubmission,
                        onDelete = { it.id?.let(viewModel::deleteQuestionPaperSubmission) }
                    )
                    2 -> PapersTab(
                        state = papersState,
                        onRetry = viewModel.questionPapers::reload,
                        permissions = permissions,
                        onEdit = {},
                        onDelete = {},
                        showApprovalActions = true,
                        onApprove = { it.id?.let(viewModel::approveQuestionPaper) },
                        onReject = { rejectingPaper = it }
                    )
                    3 -> BankTab(
                        state = bankState,
                        onRetry = viewModel.questionBank::reload,
                        canUpdate = permissions.canUpdateQuestionBankEntry,
                        canDelete = permissions.canDeleteQuestionBankEntry,
                        onEdit = { editingBankEntry = it },
                        onDelete = { it.id?.let(viewModel::deleteQuestionBankEntry) }
                    )
                }
            }
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.padding(padding)) { data ->
            Snackbar(snackbarData = data)
        }
    }

    if (showAddPaper) {
        AddQuestionPaperDialog(
            onDismiss = { showAddPaper = false },
            onSubmit = { scheduleId, courseId, content ->
                viewModel.addQuestionPaper(scheduleId, courseId, content)
                showAddPaper = false
            }
        )
    }

    editingPaper?.let { paper ->
        EditQuestionPaperDialog(
            paper = paper,
            onDismiss = { editingPaper = null },
            onSubmit = { content, status ->
                paper.id?.let { viewModel.updateQuestionPaper(it, content, status) }
                editingPaper = null
            }
        )
    }

    rejectingPaper?.let { paper ->
        RejectQuestionPaperDialog(
            onDismiss = { rejectingPaper = null },
            onSubmit = { reason ->
                paper.id?.let { viewModel.rejectQuestionPaper(it, reason) }
                rejectingPaper = null
            }
        )
    }

    if (showSubmitPaper) {
        SubmitQuestionPaperDialog(
            onDismiss = { showSubmitPaper = false },
            onSubmit = { questionPaperId ->
                viewModel.submitQuestionPaper(questionPaperId)
                showSubmitPaper = false
            }
        )
    }

    if (showAddBankEntry) {
        AddQuestionBankEntryDialog(
            onDismiss = { showAddBankEntry = false },
            onSubmit = { courseId, content, questionPaperId ->
                viewModel.addQuestionBankEntry(courseId, content, questionPaperId)
                showAddBankEntry = false
            }
        )
    }

    editingBankEntry?.let { entry ->
        EditQuestionBankEntryDialog(
            entry = entry,
            onDismiss = { editingBankEntry = null },
            onSubmit = { content ->
                entry.id?.let { viewModel.updateQuestionBankEntry(it, content) }
                editingBankEntry = null
            }
        )
    }
}

// --- Papers / Approval ---

@Composable
private fun PapersTab(
    state: UiState<List<QuestionPaper>>,
    onRetry: () -> Unit,
    permissions: ExamAdminPermissions,
    onEdit: (QuestionPaper) -> Unit,
    onDelete: (QuestionPaper) -> Unit,
    showApprovalActions: Boolean,
    onApprove: (QuestionPaper) -> Unit,
    onReject: (QuestionPaper) -> Unit
) {
    ExamSectionList(
        state = state,
        emptyMessage = "No question papers yet.",
        onRetry = onRetry,
        key = { it.id ?: it.hashCode() }
    ) { paper ->
        ResponsiveContent {
            QuestionPaperCard(
                paper = paper,
                canUpdate = !showApprovalActions && permissions.canUpdateQuestionPaper,
                canDelete = !showApprovalActions && permissions.canDeleteQuestionPaper,
                onEdit = { onEdit(paper) },
                onDelete = { onDelete(paper) },
                showApprovalActions = showApprovalActions && permissions.canApproveQuestionPaper,
                onApprove = { onApprove(paper) },
                onReject = { onReject(paper) }
            )
        }
    }
}

@Composable
private fun QuestionPaperCard(
    paper: QuestionPaper,
    canUpdate: Boolean,
    canDelete: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    showApprovalActions: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val spacing = LocalSpacing.current
    RecordCard {
        RecordRow(
            icon = Icons.Default.Description,
            title = "Course ${paper.courseId ?: "—"}",
            subtitle = "Schedule ${paper.scheduleId ?: "—"}",
            trailing = paper.status?.let { status -> { StatusPill(status) } }
        )
        if (!paper.content.isNullOrBlank()) {
            Text(
                paper.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                modifier = Modifier.padding(top = spacing.sm)
            )
        }
        if (showApprovalActions) {
            Column(modifier = Modifier.padding(top = spacing.xs)) {
                Text(
                    "Approve or reject",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    TextButton(onClick = onApprove) { Text("Approve") }
                    TextButton(onClick = onReject) {
                        Text("Reject", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        AdminRowActions(canUpdate = canUpdate, canDelete = canDelete, onUpdate = onEdit, onDelete = onDelete)
    }
}

// --- Submissions ---

@Composable
private fun SubmissionsTab(
    state: UiState<List<QuestionPaperSubmission>>,
    onRetry: () -> Unit,
    canDelete: Boolean,
    onDelete: (QuestionPaperSubmission) -> Unit
) {
    ExamSectionList(
        state = state,
        emptyMessage = "No submissions yet.",
        onRetry = onRetry,
        key = { it.id ?: it.hashCode() }
    ) { submission ->
        ResponsiveContent {
            RecordCard {
                RecordRow(
                    icon = Icons.Default.Send,
                    title = "Paper ${submission.questionPaperId ?: "—"}",
                    subtitle = submission.submittedAt?.let { "Submitted $it" },
                    trailing = submission.status?.let { status -> { StatusPill(status) } }
                )
                AdminRowActions(
                    canUpdate = false,
                    canDelete = canDelete,
                    onDelete = { onDelete(submission) }
                )
            }
        }
    }
}

// --- Bank ---

@Composable
private fun BankTab(
    state: UiState<List<QuestionBankEntry>>,
    onRetry: () -> Unit,
    canUpdate: Boolean,
    canDelete: Boolean,
    onEdit: (QuestionBankEntry) -> Unit,
    onDelete: (QuestionBankEntry) -> Unit
) {
    val spacing = LocalSpacing.current
    ExamSectionList(
        state = state,
        emptyMessage = "The question bank is empty.",
        onRetry = onRetry,
        key = { it.id ?: it.hashCode() }
    ) { entry ->
        ResponsiveContent {
            RecordCard {
                RecordRow(
                    icon = Icons.Default.LibraryBooks,
                    title = "Course ${entry.courseId ?: "—"}",
                    subtitle = entry.questionPaperId?.let { "From paper $it" }
                )
                if (!entry.content.isNullOrBlank()) {
                    Text(
                        entry.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        modifier = Modifier.padding(top = spacing.sm)
                    )
                }
                AdminRowActions(
                    canUpdate = canUpdate,
                    canDelete = canDelete,
                    onUpdate = { onEdit(entry) },
                    onDelete = { onDelete(entry) }
                )
            }
        }
    }
}

// --- Dialogs: Question Paper ---

@Composable
private fun AddQuestionPaperDialog(
    onDismiss: () -> Unit,
    onSubmit: (scheduleId: String, courseId: String, content: String) -> Unit
) {
    val spacing = LocalSpacing.current
    var scheduleId by remember { mutableStateOf("") }
    var courseId by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    val canSubmit = scheduleId.isNotBlank() && courseId.isNotBlank() && content.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Draft question paper", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PremiumTextField(text = scheduleId, onTextChange = { scheduleId = it }, placeholder = "Schedule ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = courseId, onTextChange = { courseId = it }, placeholder = "Course ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = content, onTextChange = { content = it }, placeholder = "Paper content", singleLine = false, imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(enabled = canSubmit, onClick = { onSubmit(scheduleId, courseId, content) }) { Text("Save draft") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EditQuestionPaperDialog(
    paper: QuestionPaper,
    onDismiss: () -> Unit,
    onSubmit: (content: String?, status: String?) -> Unit
) {
    val spacing = LocalSpacing.current
    var content by remember { mutableStateOf(paper.content.orEmpty()) }
    var status by remember { mutableStateOf(paper.status.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Edit question paper", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PremiumTextField(text = content, onTextChange = { content = it }, placeholder = "Paper content", singleLine = false, imeAction = ImeAction.Next)
                PremiumTextField(text = status, onTextChange = { status = it }, placeholder = "Status (optional)", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(content.ifBlank { null }, status.ifBlank { null }) }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun RejectQuestionPaperDialog(
    onDismiss: () -> Unit,
    onSubmit: (reason: String?) -> Unit
) {
    val spacing = LocalSpacing.current
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Reject question paper", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PremiumTextField(text = reason, onTextChange = { reason = it }, placeholder = "Reason (optional)", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(reason.ifBlank { null }) }) {
                Text("Reject", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// --- Dialog: Question Paper Submission ---

@Composable
private fun SubmitQuestionPaperDialog(
    onDismiss: () -> Unit,
    onSubmit: (questionPaperId: String) -> Unit
) {
    val spacing = LocalSpacing.current
    var questionPaperId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Submit for review", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PremiumTextField(text = questionPaperId, onTextChange = { questionPaperId = it }, placeholder = "Question paper ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(enabled = questionPaperId.isNotBlank(), onClick = { onSubmit(questionPaperId) }) { Text("Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// --- Dialogs: Question Bank ---

@Composable
private fun AddQuestionBankEntryDialog(
    onDismiss: () -> Unit,
    onSubmit: (courseId: String, content: String, questionPaperId: String?) -> Unit
) {
    val spacing = LocalSpacing.current
    var courseId by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var questionPaperId by remember { mutableStateOf("") }

    val canSubmit = courseId.isNotBlank() && content.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Add to question bank", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PremiumTextField(text = courseId, onTextChange = { courseId = it }, placeholder = "Course ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = content, onTextChange = { content = it }, placeholder = "Question content", singleLine = false, imeAction = ImeAction.Next)
                PremiumTextField(text = questionPaperId, onTextChange = { questionPaperId = it }, placeholder = "Source question paper ID (optional)", keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSubmit,
                onClick = { onSubmit(courseId, content, questionPaperId.ifBlank { null }) }
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EditQuestionBankEntryDialog(
    entry: QuestionBankEntry,
    onDismiss: () -> Unit,
    onSubmit: (content: String) -> Unit
) {
    val spacing = LocalSpacing.current
    var content by remember { mutableStateOf(entry.content.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Edit question bank entry", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PremiumTextField(text = content, onTextChange = { content = it }, placeholder = "Question content", singleLine = false, imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(enabled = content.isNotBlank(), onClick = { onSubmit(content) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
