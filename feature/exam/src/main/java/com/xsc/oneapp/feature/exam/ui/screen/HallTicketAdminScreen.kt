@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.exam.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
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
import com.xsc.oneapp.feature.exam.domain.model.StudentExamBlock
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

/** m_exam §4 sm_hallTicket (admin side): generating/publishing hall tickets is an
 * action form rather than a record list (there is no "all hall tickets" browse action
 * in the contract), tabbed alongside the one real list this surface owns - students
 * placed on an exam block, m_exam §4.3. */
private val TABS = listOf("Generate & Publish", "Blocked Students")

@Composable
fun HallTicketAdminScreen(onBack: () -> Unit, viewModel: ExamAdminViewModel) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var showAddBlockDialog by remember { mutableStateOf(false) }
    var editingBlock by remember { mutableStateOf<StudentExamBlock?>(null) }

    val blocksState by viewModel.examBlocksAdmin.state.collectAsStateWithLifecycle()
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadHallTicketAdmin() }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is ExamEffect.ShowToast) {
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
            }
        }
    }

    RecordScaffold(
        title = "Hall ticket administration",
        onBack = onBack,
        floatingActionButton = {
            if (selected == 1 && permissions.canAddStudentExamBlock) {
                ExtendedFloatingActionButton(
                    text = { Text("Block student") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = { showAddBlockDialog = true }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SectionChips(options = TABS, selectedIndex = selected, onSelect = { selected = it })

            Box(modifier = Modifier.fillMaxSize()) {
                if (selected == 0) {
                    GenerateAndPublishTab(
                        permissions = permissions,
                        onGenerate = { scheduleId, studentId, venueDetails, seatNumber ->
                            viewModel.generateHallTicket(scheduleId, studentId, venueDetails, seatNumber)
                        },
                        onPublish = viewModel::publishHallTickets,
                        onHold = viewModel::holdHallTickets,
                        onUnpublish = viewModel::unpublishHallTickets
                    )
                } else {
                    ExamSectionList(
                        state = blocksState,
                        emptyMessage = "No students currently blocked from an exam.",
                        onRetry = viewModel.examBlocksAdmin::reload,
                        key = { it.id ?: it.hashCode() }
                    ) { block ->
                        ResponsiveContent {
                            StudentExamBlockCard(
                                block,
                                canUpdate = permissions.canUpdateStudentExamBlock,
                                canDelete = permissions.canDeleteStudentExamBlock,
                                onUpdate = { editingBlock = block },
                                onDelete = { block.id?.let(viewModel::deleteStudentExamBlock) }
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

    if (showAddBlockDialog) {
        AddStudentExamBlockDialog(
            onDismiss = { showAddBlockDialog = false },
            onSubmit = { studentId, scheduleId, reason ->
                viewModel.addStudentExamBlock(studentId, scheduleId, reason)
                showAddBlockDialog = false
            }
        )
    }

    editingBlock?.let { block ->
        UpdateStudentExamBlockDialog(
            block = block,
            onDismiss = { editingBlock = null },
            onSubmit = { reason, status ->
                block.id?.let { viewModel.updateStudentExamBlock(it, reason, status) }
                editingBlock = null
            }
        )
    }
}

/** Two independent sections rather than one form: generating a hall ticket and
 * publishing a schedule's tickets are separate m_exam actions (`hallTicket` add vs.
 * `hallTicketPublication` publish/hold/unpublish) with their own id inputs, not steps
 * of one flow. */
@Composable
private fun GenerateAndPublishTab(
    permissions: com.xsc.oneapp.feature.exam.ui.state.ExamAdminPermissions,
    onGenerate: (scheduleId: String, studentId: String, venueDetails: String?, seatNumber: String?) -> Unit,
    onPublish: (scheduleId: String) -> Unit,
    onHold: (scheduleId: String) -> Unit,
    onUnpublish: (scheduleId: String) -> Unit
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.lg)
    ) {
        ResponsiveContent(verticalArrangement = Arrangement.spacedBy(spacing.lg)) {
            if (permissions.canGenerateHallTicket) {
                GenerateHallTicketSection(onGenerate)
            }
            if (permissions.canPublishHallTickets) {
                PublishHallTicketsSection(onPublish, onHold, onUnpublish)
            }
            if (!permissions.canGenerateHallTicket && !permissions.canPublishHallTickets) {
                Text(
                    "You don't have access to any hall ticket actions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** m_exam §4.1: hallTicket/add requires scheduleId and studentId; venueDetails and
 * seatNumber are optional (a venue may not be finalised yet). */
@Composable
private fun GenerateHallTicketSection(onGenerate: (String, String, String?, String?) -> Unit) {
    val spacing = LocalSpacing.current
    var scheduleId by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var venueDetails by remember { mutableStateOf("") }
    var seatNumber by remember { mutableStateOf("") }

    RecordCard {
        Text("Generate hall ticket", style = MaterialTheme.typography.titleMedium)
        Column(
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
            modifier = Modifier.padding(top = spacing.md)
        ) {
            PremiumTextField(text = scheduleId, onTextChange = { scheduleId = it }, placeholder = "Schedule ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            PremiumTextField(text = studentId, onTextChange = { studentId = it }, placeholder = "Student ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            PremiumTextField(text = venueDetails, onTextChange = { venueDetails = it }, placeholder = "Venue details (optional)", imeAction = ImeAction.Next)
            PremiumTextField(text = seatNumber, onTextChange = { seatNumber = it }, placeholder = "Seat number (optional)", imeAction = ImeAction.Done)
        }
        PrimaryButton(
            text = "Generate",
            onClick = { onGenerate(scheduleId, studentId, venueDetails.ifBlank { null }, seatNumber.ifBlank { null }) },
            enabled = scheduleId.isNotBlank() && studentId.isNotBlank(),
            modifier = Modifier.padding(top = spacing.md)
        )
    }
}

/** m_exam §4.2: hallTicketPublication/publish, /hold and /unpublish all act on a whole
 * schedule's tickets at once and share one admin flag, per [com.xsc.oneapp.feature.exam.ui.state.ExamAdminPermissions]. */
@Composable
private fun PublishHallTicketsSection(
    onPublish: (String) -> Unit,
    onHold: (String) -> Unit,
    onUnpublish: (String) -> Unit
) {
    val spacing = LocalSpacing.current
    var scheduleId by remember { mutableStateOf("") }

    RecordCard {
        Text("Publish hall tickets", style = MaterialTheme.typography.titleMedium)
        PremiumTextField(
            text = scheduleId,
            onTextChange = { scheduleId = it },
            placeholder = "Schedule ID",
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
            modifier = Modifier.padding(top = spacing.md)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            modifier = Modifier.fillMaxWidth().padding(top = spacing.md)
        ) {
            TextButton(onClick = { onPublish(scheduleId) }, enabled = scheduleId.isNotBlank()) { Text("Publish") }
            TextButton(onClick = { onHold(scheduleId) }, enabled = scheduleId.isNotBlank()) { Text("Hold") }
            TextButton(onClick = { onUnpublish(scheduleId) }, enabled = scheduleId.isNotBlank()) { Text("Unpublish") }
        }
    }
}

@Composable
private fun StudentExamBlockCard(
    block: StudentExamBlock,
    canUpdate: Boolean,
    canDelete: Boolean,
    onUpdate: () -> Unit,
    onDelete: () -> Unit
) {
    val spacing = LocalSpacing.current

    RecordCard {
        RecordRow(
            icon = Icons.Default.Block,
            title = "Student ${block.studentId ?: "—"}",
            subtitle = block.reason,
            trailing = block.status?.let { status -> { StatusPill(status) } }
        )
        Text(
            "Schedule ${block.scheduleId ?: "—"}" + (block.blockedBy?.let { " · Blocked by $it" } ?: ""),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = spacing.sm)
        )
        AdminRowActions(
            canUpdate = canUpdate,
            canDelete = canDelete,
            onUpdate = onUpdate,
            onDelete = onDelete
        )
    }
}

/** m_exam §4.3: studentBlock/add requires studentId, scheduleId and reason. */
@Composable
private fun AddStudentExamBlockDialog(
    onDismiss: () -> Unit,
    onSubmit: (studentId: String, scheduleId: String, reason: String) -> Unit
) {
    val spacing = LocalSpacing.current
    var studentId by remember { mutableStateOf("") }
    var scheduleId by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Block student from exam", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                PremiumTextField(text = studentId, onTextChange = { studentId = it }, placeholder = "Student ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = scheduleId, onTextChange = { scheduleId = it }, placeholder = "Schedule ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = reason, onTextChange = { reason = it }, placeholder = "Reason", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(studentId, scheduleId, reason) },
                enabled = studentId.isNotBlank() && scheduleId.isNotBlank() && reason.isNotBlank()
            ) { Text("Block") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun UpdateStudentExamBlockDialog(
    block: StudentExamBlock,
    onDismiss: () -> Unit,
    onSubmit: (reason: String?, status: String?) -> Unit
) {
    val spacing = LocalSpacing.current
    var reason by remember { mutableStateOf(block.reason.orEmpty()) }
    var status by remember { mutableStateOf(block.status.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Update block", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                PremiumTextField(text = reason, onTextChange = { reason = it }, placeholder = "Reason", imeAction = ImeAction.Next)
                PremiumTextField(text = status, onTextChange = { status = it }, placeholder = "Status", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(reason.ifBlank { null }, status.ifBlank { null }) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

