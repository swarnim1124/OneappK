@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.exam.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SupervisorAccount
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
import com.xsc.oneapp.feature.exam.domain.model.InvigilatorAssignment
import com.xsc.oneapp.feature.exam.ui.components.AdminRowActions
import com.xsc.oneapp.feature.exam.ui.components.ExamSectionList
import com.xsc.oneapp.feature.exam.ui.state.ExamEffect
import com.xsc.oneapp.feature.exam.ui.viewmodel.ExamAdminViewModel
import com.xsc.sdk.commonui.record.RecordCard
import com.xsc.sdk.commonui.record.RecordRow
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.ResponsiveContent
import com.xsc.sdk.commonui.record.StatusPill
import com.xsc.sdk.commonui.textfield.PremiumTextField
import com.xsc.sdk.theme.LocalSpacing
import kotlinx.coroutines.launch

/** m_exam §3.5 sm_invigilation/invigilatorAssignment - admin-only. Loaded via
 * [ExamAdminViewModel.loadSeatingAndInvigilation], shared with [SeatingPlanScreen]. */
@Composable
fun InvigilationScreen(onBack: () -> Unit, viewModel: ExamAdminViewModel) {
    val state by viewModel.invigilatorAssignments.state.collectAsStateWithLifecycle()
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<InvigilatorAssignment?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadSeatingAndInvigilation() }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is ExamEffect.ShowToast) {
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
            }
        }
    }

    RecordScaffold(
        title = "Invigilation",
        onBack = onBack,
        floatingActionButton = {
            if (permissions.canAddInvigilatorAssignment) {
                ExtendedFloatingActionButton(
                    text = { Text("Assign invigilator") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = { showAddDialog = true }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            ExamSectionList(
                state = state,
                emptyMessage = "No invigilators assigned yet.",
                onRetry = viewModel.invigilatorAssignments::reload,
                key = { it.id ?: it.hashCode() }
            ) { assignment ->
                ResponsiveContent {
                    InvigilatorAssignmentCard(
                        assignment,
                        canUpdate = permissions.canUpdateInvigilatorAssignment,
                        canDelete = permissions.canDeleteInvigilatorAssignment,
                        onUpdate = { editing = assignment },
                        onDelete = { assignment.id?.let(viewModel::deleteInvigilatorAssignment) }
                    )
                }
            }
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.padding(padding)) { data ->
            Snackbar(snackbarData = data)
        }
    }

    if (showAddDialog) {
        AddInvigilatorAssignmentDialog(
            onDismiss = { showAddDialog = false },
            onSubmit = { scheduleId, venueId, facultyId, dutyType ->
                viewModel.addInvigilatorAssignment(scheduleId, venueId, facultyId, dutyType)
                showAddDialog = false
            }
        )
    }

    editing?.let { assignment ->
        UpdateInvigilatorAssignmentDialog(
            assignment = assignment,
            onDismiss = { editing = null },
            onSubmit = { dutyType, status ->
                assignment.id?.let { viewModel.updateInvigilatorAssignment(it, dutyType, status) }
                editing = null
            }
        )
    }
}

@Composable
private fun InvigilatorAssignmentCard(
    assignment: InvigilatorAssignment,
    canUpdate: Boolean,
    canDelete: Boolean,
    onUpdate: () -> Unit,
    onDelete: () -> Unit
) {
    RecordCard {
        RecordRow(
            icon = Icons.Default.SupervisorAccount,
            title = "Faculty ${assignment.facultyId ?: "—"} · ${assignment.dutyType ?: "Duty"}",
            subtitle = "Schedule ${assignment.scheduleId ?: "—"} · Venue ${assignment.venueId ?: "—"}",
            trailing = assignment.status?.let { status -> { StatusPill(status) } }
        )
        AdminRowActions(
            canUpdate = canUpdate,
            canDelete = canDelete,
            onUpdate = onUpdate,
            onDelete = onDelete
        )
    }
}

/** m_exam §3.5: invigilatorAssignment/add requires scheduleId, venueId, facultyId and
 * dutyType. */
@Composable
private fun AddInvigilatorAssignmentDialog(
    onDismiss: () -> Unit,
    onSubmit: (scheduleId: String, venueId: String, facultyId: String, dutyType: String) -> Unit
) {
    val spacing = LocalSpacing.current
    var scheduleId by remember { mutableStateOf("") }
    var venueId by remember { mutableStateOf("") }
    var facultyId by remember { mutableStateOf("") }
    var dutyType by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Assign invigilator", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                PremiumTextField(text = scheduleId, onTextChange = { scheduleId = it }, placeholder = "Schedule ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = venueId, onTextChange = { venueId = it }, placeholder = "Venue ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = facultyId, onTextChange = { facultyId = it }, placeholder = "Faculty ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = dutyType, onTextChange = { dutyType = it }, placeholder = "Duty type", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(scheduleId, venueId, facultyId, dutyType) },
                enabled = scheduleId.isNotBlank() && venueId.isNotBlank() && facultyId.isNotBlank() && dutyType.isNotBlank()
            ) { Text("Assign") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun UpdateInvigilatorAssignmentDialog(
    assignment: InvigilatorAssignment,
    onDismiss: () -> Unit,
    onSubmit: (dutyType: String?, status: String?) -> Unit
) {
    val spacing = LocalSpacing.current
    var dutyType by remember { mutableStateOf(assignment.dutyType.orEmpty()) }
    var status by remember { mutableStateOf(assignment.status.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Update assignment", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                PremiumTextField(text = dutyType, onTextChange = { dutyType = it }, placeholder = "Duty type", imeAction = ImeAction.Next)
                PremiumTextField(text = status, onTextChange = { status = it }, placeholder = "Status", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(dutyType.ifBlank { null }, status.ifBlank { null }) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
