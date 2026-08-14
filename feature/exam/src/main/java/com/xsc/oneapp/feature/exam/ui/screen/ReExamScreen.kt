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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.exam.domain.model.ReappearExam
import com.xsc.oneapp.feature.exam.domain.model.SupplementaryExam
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
import kotlinx.coroutines.launch

/** m_exam §8.2-8.3: supplementary (backlog) and reappear/improvement exams, tabbed for
 * the same reason Revaluation is - two sessions of one "re-sit a course" story, not two
 * unrelated features. */
private val TABS = listOf("Supplementary", "Reappear")

/** What "Register" was tapped on - carries just enough to drive [RegisterDialog]. The
 * actual submit call is resolved by [ReExamScreen] (the only place that knows which
 * tab, and therefore which use case, a tap came from), never built inside a card. */
private data class RegisterTarget(val id: String, val name: String, val courses: List<String>)

@Composable
fun ReExamScreen(onBack: () -> Unit, viewModel: ExamViewModel, adminViewModel: ExamAdminViewModel) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var registering by remember { mutableStateOf<RegisterTarget?>(null) }
    var showCreateSupplementary by remember { mutableStateOf(false) }
    var showCreateReappear by remember { mutableStateOf(false) }
    var settingEligibilityFor by remember { mutableStateOf<String?>(null) }

    val supplementaryState by viewModel.supplementaryExams.state.collectAsStateWithLifecycle()
    val reappearState by viewModel.reappearExams.state.collectAsStateWithLifecycle()
    val adminPermissions by adminViewModel.permissions.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadReExams() }
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
        title = "Re-exams",
        onBack = onBack,
        floatingActionButton = {
            when (selected) {
                0 -> if (adminPermissions.canCreateSupplementaryExam) {
                    ExtendedFloatingActionButton(
                        text = { Text("Schedule exam") },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = { showCreateSupplementary = true }
                    )
                }
                else -> if (adminPermissions.canCreateReappearExam) {
                    ExtendedFloatingActionButton(
                        text = { Text("Schedule exam") },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = { showCreateReappear = true }
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SectionChips(options = TABS, selectedIndex = selected, onSelect = { selected = it })

            Box(modifier = Modifier.fillMaxSize()) {
                if (selected == 0) {
                    ExamSectionList(
                        state = supplementaryState,
                        emptyMessage = "No supplementary exams scheduled.",
                        onRetry = viewModel.supplementaryExams::reload,
                        key = { it.id ?: it.hashCode() }
                    ) { exam ->
                        ResponsiveContent { SupplementaryExamCard(exam, onRegister = { registering = it }) }
                    }
                } else {
                    ExamSectionList(
                        state = reappearState,
                        emptyMessage = "No reappear exams scheduled.",
                        onRetry = viewModel.reappearExams::reload,
                        key = { it.id ?: it.hashCode() }
                    ) { exam ->
                        ResponsiveContent {
                            ReappearExamCard(
                                exam,
                                onRegister = { registering = it },
                                canSetEligibility = adminPermissions.canSetReappearEligibility,
                                onSetEligibility = { exam.id?.let { settingEligibilityFor = it } }
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.padding(padding)) { data ->
            Snackbar(snackbarData = data)
        }
    }

    // Which use case fires depends on which tab was showing when "Register" was
    // tapped - selected doesn't change while the dialog is open, so it's safe to read
    // here rather than threading a second discriminator through RegisterTarget.
    registering?.let { target ->
        RegisterDialog(
            target = target,
            onDismiss = { registering = null },
            onSubmit = { courseIds ->
                if (selected == 0) {
                    viewModel.registerForSupplementaryExam(target.id, courseIds)
                } else {
                    viewModel.registerForReappearExam(target.id, courseIds)
                }
                registering = null
            }
        )
    }

    if (showCreateSupplementary) {
        CreateSupplementaryExamDialog(
            onDismiss = { showCreateSupplementary = false },
            onSubmit = { name, fromDate, toDate, courseIds ->
                adminViewModel.createSupplementaryExam(name, fromDate, toDate, courseIds)
                showCreateSupplementary = false
                viewModel.supplementaryExams.reload()
            }
        )
    }

    if (showCreateReappear) {
        CreateReappearExamDialog(
            onDismiss = { showCreateReappear = false },
            onSubmit = { name, termId, startDate, endDate, regStart, regEnd, courseIds ->
                adminViewModel.createReappearExam(name, termId, startDate, endDate, regStart, regEnd, courseIds)
                showCreateReappear = false
                viewModel.reappearExams.reload()
            }
        )
    }

    settingEligibilityFor?.let { reappearExamId ->
        SetReappearEligibilityDialog(
            onDismiss = { settingEligibilityFor = null },
            onSubmit = { studentId, courseId, isEligible, reason ->
                adminViewModel.setReappearEligibility(studentId, courseId, reappearExamId, isEligible, reason)
                settingEligibilityFor = null
            }
        )
    }
}

@Composable
private fun SupplementaryExamCard(exam: SupplementaryExam, onRegister: (RegisterTarget) -> Unit) {
    val spacing = LocalSpacing.current
    val id = exam.id

    RecordCard {
        RecordRow(
            icon = Icons.Default.CalendarToday,
            title = exam.name ?: "Supplementary exam",
            subtitle = dateRange(exam.fromDate, exam.toDate),
            trailing = exam.status?.let { status -> { StatusPill(status) } }
        )
        if (id != null) {
            TextButton(
                onClick = { onRegister(RegisterTarget(id, exam.name ?: "Supplementary exam", exam.courses)) },
                modifier = Modifier.padding(top = spacing.xs)
            ) {
                Icon(Icons.Default.HowToReg, contentDescription = null)
                Text("Register", modifier = Modifier.padding(start = spacing.xs))
            }
        }
    }
}

@Composable
private fun ReappearExamCard(
    exam: ReappearExam,
    onRegister: (RegisterTarget) -> Unit,
    canSetEligibility: Boolean,
    onSetEligibility: () -> Unit
) {
    val spacing = LocalSpacing.current
    val id = exam.id

    RecordCard {
        RecordRow(
            icon = Icons.Default.EditCalendar,
            title = exam.name ?: "Reappear exam",
            subtitle = dateRange(exam.examStartDate, exam.examEndDate),
            trailing = exam.status?.let { status -> { StatusPill(status) } }
        )
        if (exam.registrationStartDate != null || exam.registrationEndDate != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                modifier = Modifier.padding(top = spacing.sm)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "Registration: ${dateRange(exam.registrationStartDate, exam.registrationEndDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (id != null) {
            TextButton(
                onClick = { onRegister(RegisterTarget(id, exam.name ?: "Reappear exam", exam.courses)) },
                modifier = Modifier.padding(top = spacing.xs)
            ) {
                Icon(Icons.Default.HowToReg, contentDescription = null)
                Text("Register", modifier = Modifier.padding(start = spacing.xs))
            }
        }
        if (canSetEligibility && id != null) {
            TextButton(onClick = onSetEligibility, modifier = Modifier.padding(top = spacing.xs)) {
                Text("Set eligibility")
            }
        }
    }
}

private fun dateRange(from: String?, to: String?): String? =
    if (from == null && to == null) null else "${from ?: "?"} – ${to ?: "?"}"

/** m_exam §8.2/8.3: both registration actions require studentId (added by the
 * ViewModel), the session id and a non-empty courseIds array. [target.courses] backs a
 * checklist when the session lists its own offered courses; otherwise falls back to a
 * comma-separated free-text field so registration is still possible. */
@Composable
private fun RegisterDialog(
    target: RegisterTarget,
    onDismiss: () -> Unit,
    onSubmit: (courseIds: List<String>) -> Unit
) {
    val spacing = LocalSpacing.current
    var selectedCourses by remember { mutableStateOf(setOf<String>()) }
    var freeText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Register - ${target.name}", style = MaterialTheme.typography.headlineSmall) },
        text = {
            if (target.courses.isNotEmpty()) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    target.courses.forEach { courseId ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = courseId in selectedCourses,
                                onCheckedChange = { checked ->
                                    selectedCourses = if (checked) {
                                        selectedCourses + courseId
                                    } else {
                                        selectedCourses - courseId
                                    }
                                }
                            )
                            Text("Course $courseId", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            } else {
                PremiumTextField(
                    text = freeText,
                    onTextChange = { freeText = it },
                    placeholder = "Course IDs, comma-separated",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            }
        },
        confirmButton = {
            val courseIds = if (target.courses.isNotEmpty()) {
                selectedCourses.toList()
            } else {
                freeText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }
            TextButton(
                onClick = { onSubmit(courseIds) },
                enabled = courseIds.isNotEmpty()
            ) { Text("Register") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** m_exam §8.2: supplementaryExam/add - schedules the session itself, admin-only. */
@Composable
private fun CreateSupplementaryExamDialog(
    onDismiss: () -> Unit,
    onSubmit: (name: String, fromDate: String, toDate: String, courseIds: List<String>) -> Unit
) {
    val spacing = LocalSpacing.current
    var name by remember { mutableStateOf("") }
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var courseIdsText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule supplementary exam") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                PremiumTextField(text = name, onTextChange = { name = it }, placeholder = "Exam name", imeAction = ImeAction.Next)
                PremiumTextField(text = fromDate, onTextChange = { fromDate = it }, placeholder = "From date (YYYY-MM-DD)", imeAction = ImeAction.Next)
                PremiumTextField(text = toDate, onTextChange = { toDate = it }, placeholder = "To date (YYYY-MM-DD)", imeAction = ImeAction.Next)
                PremiumTextField(text = courseIdsText, onTextChange = { courseIdsText = it }, placeholder = "Course IDs, comma-separated", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            val courseIds = courseIdsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            TextButton(
                onClick = { onSubmit(name, fromDate, toDate, courseIds) },
                enabled = name.isNotBlank() && fromDate.isNotBlank() && toDate.isNotBlank() && courseIds.isNotEmpty()
            ) { Text("Schedule") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** m_exam §8.3: reappearExam/add - schedules the session itself, admin-only. */
@Composable
private fun CreateReappearExamDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        name: String,
        academicTermId: String,
        examStartDate: String,
        examEndDate: String,
        registrationStartDate: String,
        registrationEndDate: String,
        courseIds: List<String>
    ) -> Unit
) {
    val spacing = LocalSpacing.current
    var name by remember { mutableStateOf("") }
    var academicTermId by remember { mutableStateOf("") }
    var examStartDate by remember { mutableStateOf("") }
    var examEndDate by remember { mutableStateOf("") }
    var registrationStartDate by remember { mutableStateOf("") }
    var registrationEndDate by remember { mutableStateOf("") }
    var courseIdsText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule reappear exam") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                PremiumTextField(text = name, onTextChange = { name = it }, placeholder = "Exam name", imeAction = ImeAction.Next)
                PremiumTextField(text = academicTermId, onTextChange = { academicTermId = it }, placeholder = "Academic term ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = examStartDate, onTextChange = { examStartDate = it }, placeholder = "Exam start date (YYYY-MM-DD)", imeAction = ImeAction.Next)
                PremiumTextField(text = examEndDate, onTextChange = { examEndDate = it }, placeholder = "Exam end date (YYYY-MM-DD)", imeAction = ImeAction.Next)
                PremiumTextField(text = registrationStartDate, onTextChange = { registrationStartDate = it }, placeholder = "Registration start (YYYY-MM-DD)", imeAction = ImeAction.Next)
                PremiumTextField(text = registrationEndDate, onTextChange = { registrationEndDate = it }, placeholder = "Registration end (YYYY-MM-DD)", imeAction = ImeAction.Next)
                PremiumTextField(text = courseIdsText, onTextChange = { courseIdsText = it }, placeholder = "Course IDs, comma-separated", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            val courseIds = courseIdsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            TextButton(
                onClick = {
                    onSubmit(
                        name, academicTermId, examStartDate, examEndDate,
                        registrationStartDate, registrationEndDate, courseIds
                    )
                },
                enabled = name.isNotBlank() && academicTermId.isNotBlank() &&
                    examStartDate.isNotBlank() && examEndDate.isNotBlank() && courseIds.isNotEmpty()
            ) { Text("Schedule") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** m_exam §8.3: reappearEligibility/add - sets the eligibility rule for a
 * student/course against the reappear session the row this dialog was opened from
 * represents, admin-only. */
@Composable
private fun SetReappearEligibilityDialog(
    onDismiss: () -> Unit,
    onSubmit: (studentId: String, courseId: String, isEligible: Boolean, reason: String?) -> Unit
) {
    val spacing = LocalSpacing.current
    var studentId by remember { mutableStateOf("") }
    var courseId by remember { mutableStateOf("") }
    var isEligible by remember { mutableStateOf(true) }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set reappear eligibility") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                PremiumTextField(text = studentId, onTextChange = { studentId = it }, placeholder = "Student ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = courseId, onTextChange = { courseId = it }, placeholder = "Course ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isEligible, onCheckedChange = { isEligible = it })
                    Text("Eligible", style = MaterialTheme.typography.bodyLarge)
                }
                PremiumTextField(text = reason, onTextChange = { reason = it }, placeholder = "Reason (optional)", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(studentId, courseId, isEligible, reason.ifBlank { null }) },
                enabled = studentId.isNotBlank() && courseId.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
