@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.exam.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.School
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
import com.xsc.oneapp.feature.exam.domain.model.Grade
import com.xsc.oneapp.feature.exam.domain.model.MarksRecord
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

/** m_exam §6.2 sm_marks: end-semester marks entry through to locked grades, tabbed as
 * the four sequential stages of one pipeline (enter -> verify -> generate grades ->
 * lock) rather than four destinations - same call already made for [ReExamScreen] and
 * [RevaluationScreen]. Verify/Lock have no list of their own to browse (the contract
 * documents no separate view action for either action's own history), so those two
 * tabs are a bare gated action form instead of an [ExamSectionList]. */
private val TABS = listOf("Marks Entry", "Verify", "Grades", "Lock")

@Composable
fun MarksGradingScreen(onBack: () -> Unit, viewModel: ExamAdminViewModel) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var showSubmitMarks by remember { mutableStateOf(false) }
    var showGenerateGrades by remember { mutableStateOf(false) }

    val marksState by viewModel.marksEntries.state.collectAsStateWithLifecycle()
    val gradesState by viewModel.grades.state.collectAsStateWithLifecycle()
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadMarksGradingAdmin() }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is ExamEffect.ShowToast) {
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
            }
        }
    }

    RecordScaffold(
        title = "Marks & grading",
        onBack = onBack,
        floatingActionButton = {
            when (selected) {
                0 -> if (permissions.canSubmitMarksEntry) {
                    ExtendedFloatingActionButton(
                        text = { Text("Submit marks") },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = { showSubmitMarks = true }
                    )
                }
                2 -> if (permissions.canGenerateGrades) {
                    ExtendedFloatingActionButton(
                        text = { Text("Generate grades") },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = { showGenerateGrades = true }
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
                        state = marksState,
                        emptyMessage = "No marks submitted yet.",
                        onRetry = viewModel.marksEntries::reload,
                        key = { it.id ?: it.hashCode() }
                    ) { record -> ResponsiveContent { MarksRecordCard(record) } }
                    1 -> VerifyMarksTab(
                        canVerify = permissions.canVerifyMarks,
                        onVerify = viewModel::verifyMarks
                    )
                    2 -> ExamSectionList(
                        state = gradesState,
                        emptyMessage = "No grades generated yet.",
                        onRetry = viewModel.grades::reload,
                        key = { it.id ?: it.hashCode() }
                    ) { grade -> ResponsiveContent { GradeCard(grade) } }
                    else -> LockGradesTab(
                        canLock = permissions.canLockGrades,
                        onLock = viewModel::lockGrades
                    )
                }
            }
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.padding(padding)) { data ->
            Snackbar(snackbarData = data)
        }
    }

    if (showSubmitMarks) {
        SubmitMarksDialog(
            onDismiss = { showSubmitMarks = false },
            onSubmit = { scheduleId, courseId, studentId, marks ->
                viewModel.submitMarksEntry(scheduleId, courseId, listOf(studentId to marks))
                showSubmitMarks = false
            }
        )
    }

    if (showGenerateGrades) {
        ScheduleCourseDialog(
            title = "Generate grades",
            confirmLabel = "Generate",
            onDismiss = { showGenerateGrades = false },
            onSubmit = { scheduleId, courseId ->
                viewModel.generateGrades(scheduleId, courseId)
                showGenerateGrades = false
            }
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
private fun GradeCard(grade: Grade) {
    RecordCard {
        RecordRow(
            icon = Icons.Default.School,
            title = "Student ${grade.studentId ?: "—"} · Grade ${grade.grade ?: "—"}",
            subtitle = "Schedule ${grade.scheduleId ?: "—"} · Course ${grade.courseId ?: "—"}",
            trailing = grade.status?.let { status -> { StatusPill(status) } }
        )
    }
}

@Composable
private fun VerifyMarksTab(canVerify: Boolean, onVerify: (scheduleId: String, courseId: String) -> Unit) {
    ScheduleCourseActionForm(
        description = "Verify all marks submitted for a schedule and course before grades can be generated.",
        buttonLabel = "Verify",
        deniedMessage = "You don't have permission to verify marks.",
        canAct = canVerify,
        onAct = onVerify
    )
}

@Composable
private fun LockGradesTab(canLock: Boolean, onLock: (scheduleId: String, courseId: String) -> Unit) {
    ScheduleCourseActionForm(
        description = "Lock generated grades for a schedule and course so they can no longer be regenerated.",
        buttonLabel = "Lock grades",
        deniedMessage = "You don't have permission to lock grades.",
        canAct = canLock,
        onAct = onLock
    )
}

/** Shared shape for [VerifyMarksTab] and [LockGradesTab]: both act on a bare
 * scheduleId/courseId pair with no list of their own to browse. */
@Composable
private fun ScheduleCourseActionForm(
    description: String,
    buttonLabel: String,
    deniedMessage: String,
    canAct: Boolean,
    onAct: (scheduleId: String, courseId: String) -> Unit
) {
    val spacing = LocalSpacing.current
    var scheduleId by remember { mutableStateOf("") }
    var courseId by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        ResponsiveContent {
            Column(
                modifier = Modifier.padding(spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                PremiumTextField(text = scheduleId, onTextChange = { scheduleId = it }, placeholder = "Schedule ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = courseId, onTextChange = { courseId = it }, placeholder = "Course ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                if (canAct) {
                    PrimaryButton(
                        text = buttonLabel,
                        onClick = { onAct(scheduleId, courseId) },
                        enabled = scheduleId.isNotBlank() && courseId.isNotBlank()
                    )
                } else {
                    Text(deniedMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/** m_exam §6.2: marksEntry/add takes a `records[]` of `{studentId, marks}` per
 * schedule/course - same one-pair-at-a-time simplification as
 * [InternalExamScreen]'s marks dialog, and for the same reason: a full per-student
 * marks table is out of scope for this pass, so this records one student's mark at a
 * time, wrapped in a one-element list. */
@Composable
private fun SubmitMarksDialog(
    onDismiss: () -> Unit,
    onSubmit: (scheduleId: String, courseId: String, studentId: String, marks: String) -> Unit
) {
    val spacing = LocalSpacing.current
    var scheduleId by remember { mutableStateOf("") }
    var courseId by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var marks by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Submit marks", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                PremiumTextField(text = scheduleId, onTextChange = { scheduleId = it }, placeholder = "Schedule ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = courseId, onTextChange = { courseId = it }, placeholder = "Course ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = studentId, onTextChange = { studentId = it }, placeholder = "Student ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = marks, onTextChange = { marks = it }, placeholder = "Marks", keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(scheduleId, courseId, studentId, marks) },
                enabled = scheduleId.isNotBlank() && courseId.isNotBlank() && studentId.isNotBlank() && marks.isNotBlank()
            ) { Text("Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** Shared scheduleId/courseId prompt - backs "Generate grades" here. */
@Composable
private fun ScheduleCourseDialog(
    title: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onSubmit: (scheduleId: String, courseId: String) -> Unit
) {
    val spacing = LocalSpacing.current
    var scheduleId by remember { mutableStateOf("") }
    var courseId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                PremiumTextField(text = scheduleId, onTextChange = { scheduleId = it }, placeholder = "Schedule ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = courseId, onTextChange = { courseId = it }, placeholder = "Course ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(scheduleId, courseId) },
                enabled = scheduleId.isNotBlank() && courseId.isNotBlank()
            ) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
