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
import androidx.compose.material.icons.filled.Grade
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
import com.xsc.oneapp.feature.exam.domain.model.InternalExam
import com.xsc.oneapp.feature.exam.domain.model.MarksRecord
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

/** m_exam §6.1 sm_internalExam: CIE exam components (Exams), per-student marks capture
 * (Marks Entry) and the roll-up into a course offering's final internal mark
 * (Consolidation), tabbed for the same "one workflow, three stages" reason
 * [ReExamScreen] and [RevaluationScreen] already tab their own families. */
private val TABS = listOf("Exams", "Marks Entry", "Consolidation")

@Composable
fun InternalExamScreen(onBack: () -> Unit, viewModel: ExamAdminViewModel) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var showAddExam by remember { mutableStateOf(false) }
    var editingExam by remember { mutableStateOf<InternalExam?>(null) }
    var showSubmitMarks by remember { mutableStateOf(false) }

    val examsState by viewModel.internalExams.state.collectAsStateWithLifecycle()
    val marksState by viewModel.internalMarksEntries.state.collectAsStateWithLifecycle()
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadInternalExamAdmin() }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is ExamEffect.ShowToast) {
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
            }
        }
    }

    RecordScaffold(
        title = "Internal exams (CIE)",
        onBack = onBack,
        floatingActionButton = {
            when (selected) {
                0 -> if (permissions.canAddInternalExam) {
                    ExtendedFloatingActionButton(
                        text = { Text("Add exam") },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = { showAddExam = true }
                    )
                }
                1 -> if (permissions.canSubmitInternalMarksEntry) {
                    ExtendedFloatingActionButton(
                        text = { Text("Submit marks") },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = { showSubmitMarks = true }
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SectionChips(options = TABS, selectedIndex = selected, onSelect = { selected = it })

            Box(modifier = Modifier.fillMaxSize()) {
                when (selected) {
                    0 -> ExamSectionList(
                        state = examsState,
                        emptyMessage = "No internal exams set up yet.",
                        onRetry = viewModel.internalExams::reload,
                        key = { it.id ?: it.hashCode() }
                    ) { exam ->
                        ResponsiveContent {
                            InternalExamCard(
                                exam,
                                canUpdate = permissions.canUpdateInternalExam,
                                canDelete = permissions.canDeleteInternalExam,
                                onUpdate = { editingExam = exam },
                                onDelete = { exam.id?.let(viewModel::deleteInternalExam) }
                            )
                        }
                    }
                    1 -> ExamSectionList(
                        state = marksState,
                        emptyMessage = "No internal marks submitted yet.",
                        onRetry = viewModel.internalMarksEntries::reload,
                        key = { it.id ?: it.hashCode() }
                    ) { record -> ResponsiveContent { MarksRecordCard(record) } }
                    else -> ConsolidationTab(
                        canConsolidate = permissions.canConsolidateInternalMarks,
                        onConsolidate = viewModel::consolidateInternalMarks
                    )
                }
            }
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.padding(padding)) { data ->
            Snackbar(snackbarData = data)
        }
    }

    if (showAddExam) {
        AddInternalExamDialog(
            onDismiss = { showAddExam = false },
            onSubmit = { courseOfferingId, examName, examTypeId, examDate, totalMaxMarks ->
                viewModel.addInternalExam(courseOfferingId, examName, examTypeId, examDate, totalMaxMarks)
                showAddExam = false
            }
        )
    }

    editingExam?.let { exam ->
        UpdateInternalExamDialog(
            exam = exam,
            onDismiss = { editingExam = null },
            onSubmit = { examName, examDate, totalMaxMarks, status ->
                exam.id?.let { viewModel.updateInternalExam(it, examName, examDate, totalMaxMarks, status) }
                editingExam = null
            }
        )
    }

    if (showSubmitMarks) {
        SubmitInternalMarksDialog(
            onDismiss = { showSubmitMarks = false },
            onSubmit = { internalExamId, studentId, marks ->
                viewModel.submitInternalMarksEntry(internalExamId, listOf(studentId to marks))
                showSubmitMarks = false
            }
        )
    }
}

@Composable
private fun InternalExamCard(
    exam: InternalExam,
    canUpdate: Boolean,
    canDelete: Boolean,
    onUpdate: () -> Unit,
    onDelete: () -> Unit
) {
    RecordCard {
        RecordRow(
            icon = Icons.Default.Assignment,
            title = exam.examName ?: "Internal exam",
            subtitle = "Course offering ${exam.courseOfferingId ?: "—"} · Type ${exam.examTypeId ?: "—"} · " +
                "${exam.examDate ?: "—"} · Max ${exam.totalMaxMarks ?: "—"}",
            trailing = exam.status?.let { status -> { StatusPill(status) } }
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
private fun MarksRecordCard(record: MarksRecord) {
    RecordCard {
        RecordRow(
            icon = Icons.Default.Grade,
            title = "Student ${record.studentId ?: "—"}",
            subtitle = "Schedule ${record.scheduleId ?: "—"} · Course ${record.courseId ?: "—"} · " +
                "Marks ${record.marks ?: "—"}",
            trailing = record.status?.let { status -> { StatusPill(status) } }
        )
    }
}

@Composable
private fun ConsolidationTab(canConsolidate: Boolean, onConsolidate: (courseOfferingId: String) -> Unit) {
    val spacing = LocalSpacing.current
    var courseOfferingId by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        ResponsiveContent {
            Column(
                modifier = Modifier.padding(spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                Text(
                    "Roll a course offering's internal exam components up into its final CIE mark.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PremiumTextField(
                    text = courseOfferingId,
                    onTextChange = { courseOfferingId = it },
                    placeholder = "Course offering ID",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
                if (canConsolidate) {
                    PrimaryButton(
                        text = "Consolidate",
                        onClick = { onConsolidate(courseOfferingId) },
                        enabled = courseOfferingId.isNotBlank()
                    )
                } else {
                    Text(
                        "You don't have permission to consolidate internal marks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** m_exam §6.1: internalExam/add requires courseOfferingId, examName, examTypeId,
 * examDate and totalMaxMarks - all mandatory per the contract's own add payload. */
@Composable
private fun AddInternalExamDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        courseOfferingId: String,
        examName: String,
        examTypeId: String,
        examDate: String,
        totalMaxMarks: String
    ) -> Unit
) {
    val spacing = LocalSpacing.current
    var courseOfferingId by remember { mutableStateOf("") }
    var examName by remember { mutableStateOf("") }
    var examTypeId by remember { mutableStateOf("") }
    var examDate by remember { mutableStateOf("") }
    var totalMaxMarks by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Add internal exam", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                PremiumTextField(text = courseOfferingId, onTextChange = { courseOfferingId = it }, placeholder = "Course offering ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = examName, onTextChange = { examName = it }, placeholder = "Exam name", imeAction = ImeAction.Next)
                PremiumTextField(text = examTypeId, onTextChange = { examTypeId = it }, placeholder = "Exam type ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = examDate, onTextChange = { examDate = it }, placeholder = "Exam date (YYYY-MM-DD)", imeAction = ImeAction.Next)
                PremiumTextField(text = totalMaxMarks, onTextChange = { totalMaxMarks = it }, placeholder = "Total max marks", keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(courseOfferingId, examName, examTypeId, examDate, totalMaxMarks) },
                enabled = courseOfferingId.isNotBlank() && examName.isNotBlank() &&
                    examTypeId.isNotBlank() && examDate.isNotBlank() && totalMaxMarks.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun UpdateInternalExamDialog(
    exam: InternalExam,
    onDismiss: () -> Unit,
    onSubmit: (examName: String?, examDate: String?, totalMaxMarks: String?, status: String?) -> Unit
) {
    val spacing = LocalSpacing.current
    var examName by remember { mutableStateOf(exam.examName.orEmpty()) }
    var examDate by remember { mutableStateOf(exam.examDate.orEmpty()) }
    var totalMaxMarks by remember { mutableStateOf(exam.totalMaxMarks.orEmpty()) }
    var status by remember { mutableStateOf(exam.status.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Update internal exam", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                PremiumTextField(text = examName, onTextChange = { examName = it }, placeholder = "Exam name", imeAction = ImeAction.Next)
                PremiumTextField(text = examDate, onTextChange = { examDate = it }, placeholder = "Exam date (YYYY-MM-DD)", imeAction = ImeAction.Next)
                PremiumTextField(text = totalMaxMarks, onTextChange = { totalMaxMarks = it }, placeholder = "Total max marks", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = status, onTextChange = { status = it }, placeholder = "Status", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSubmit(
                        examName.ifBlank { null },
                        examDate.ifBlank { null },
                        totalMaxMarks.ifBlank { null },
                        status.ifBlank { null }
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** m_exam §6.1: internalMarksEntry/add takes a `records[]` of `{studentId, marks}` per
 * internal exam. A real per-student marks table (add-row/remove-row for a whole class
 * list at once) is out of scope for this pass, so this dialog accepts exactly one
 * studentId/marks pair and wraps it in a one-element list - each submission records one
 * student's mark, not a batch. */
@Composable
private fun SubmitInternalMarksDialog(
    onDismiss: () -> Unit,
    onSubmit: (internalExamId: String, studentId: String, marks: String) -> Unit
) {
    val spacing = LocalSpacing.current
    var internalExamId by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var marks by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Submit internal marks", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                PremiumTextField(text = internalExamId, onTextChange = { internalExamId = it }, placeholder = "Internal exam ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = studentId, onTextChange = { studentId = it }, placeholder = "Student ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = marks, onTextChange = { marks = it }, placeholder = "Marks", keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(internalExamId, studentId, marks) },
                enabled = internalExamId.isNotBlank() && studentId.isNotBlank() && marks.isNotBlank()
            ) { Text("Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
