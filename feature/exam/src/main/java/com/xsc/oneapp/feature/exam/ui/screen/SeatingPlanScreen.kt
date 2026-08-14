@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.exam.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventSeat
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
import com.xsc.oneapp.feature.exam.domain.model.SeatingPlan
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

/** m_exam §3.4 sm_seating/seatingPlan - admin-only, no student-facing equivalent since
 * [com.xsc.oneapp.feature.exam.domain.model.HallTicket] already carries a student's own
 * seat. Loaded via [ExamAdminViewModel.loadSeatingAndInvigilation], which also warms
 * [InvigilationScreen]'s list - both screens sharing one graph-scoped ViewModel means
 * whichever opens first populates the other for free. */
@Composable
fun SeatingPlanScreen(onBack: () -> Unit, viewModel: ExamAdminViewModel) {
    val state by viewModel.seatingPlans.state.collectAsStateWithLifecycle()
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<SeatingPlan?>(null) }

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
        title = "Seating plan",
        onBack = onBack,
        floatingActionButton = {
            if (permissions.canAddSeatingPlan) {
                ExtendedFloatingActionButton(
                    text = { Text("Allocate seat") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = { showAddDialog = true }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            ExamSectionList(
                state = state,
                emptyMessage = "No seats allocated yet.",
                onRetry = viewModel.seatingPlans::reload,
                key = { it.id ?: it.hashCode() }
            ) { plan ->
                ResponsiveContent {
                    SeatingPlanCard(
                        plan,
                        canUpdate = permissions.canUpdateSeatingPlan,
                        canDelete = permissions.canDeleteSeatingPlan,
                        onUpdate = { editing = plan },
                        onDelete = { plan.id?.let(viewModel::deleteSeatingPlan) }
                    )
                }
            }
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.padding(padding)) { data ->
            Snackbar(snackbarData = data)
        }
    }

    if (showAddDialog) {
        AddSeatingPlanDialog(
            onDismiss = { showAddDialog = false },
            onSubmit = { scheduleId, venueId, courseId, studentId, seatNumber, allocationMethod ->
                viewModel.addSeatingPlan(scheduleId, venueId, courseId, studentId, seatNumber, allocationMethod)
                showAddDialog = false
            }
        )
    }

    editing?.let { plan ->
        UpdateSeatingPlanDialog(
            plan = plan,
            onDismiss = { editing = null },
            onSubmit = { seatNumber, venueId ->
                plan.id?.let { viewModel.updateSeatingPlan(it, seatNumber, venueId) }
                editing = null
            }
        )
    }
}

@Composable
private fun SeatingPlanCard(
    plan: SeatingPlan,
    canUpdate: Boolean,
    canDelete: Boolean,
    onUpdate: () -> Unit,
    onDelete: () -> Unit
) {
    RecordCard {
        RecordRow(
            icon = Icons.Default.EventSeat,
            title = "Seat ${plan.seatNumber ?: "—"}",
            subtitle = "Schedule ${plan.scheduleId ?: "—"} · Venue ${plan.venueId ?: "—"} · " +
                "Course ${plan.courseId ?: "—"} · Student ${plan.studentId ?: "—"}",
            trailing = plan.allocationMethod?.let { method -> { StatusPill(method) } }
        )
        AdminRowActions(
            canUpdate = canUpdate,
            canDelete = canDelete,
            onUpdate = onUpdate,
            onDelete = onDelete
        )
    }
}

/** m_exam §3.4: seatingPlan/add requires scheduleId, venueId, courseId, studentId and
 * seatNumber; allocationMethod is optional (manual vs. auto-allocated). */
@Composable
private fun AddSeatingPlanDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        scheduleId: String,
        venueId: String,
        courseId: String,
        studentId: String,
        seatNumber: String,
        allocationMethod: String?
    ) -> Unit
) {
    val spacing = LocalSpacing.current
    var scheduleId by remember { mutableStateOf("") }
    var venueId by remember { mutableStateOf("") }
    var courseId by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var seatNumber by remember { mutableStateOf("") }
    var allocationMethod by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Allocate seat", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                PremiumTextField(text = scheduleId, onTextChange = { scheduleId = it }, placeholder = "Schedule ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = venueId, onTextChange = { venueId = it }, placeholder = "Venue ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = courseId, onTextChange = { courseId = it }, placeholder = "Course ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = studentId, onTextChange = { studentId = it }, placeholder = "Student ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = seatNumber, onTextChange = { seatNumber = it }, placeholder = "Seat number", imeAction = ImeAction.Next)
                PremiumTextField(text = allocationMethod, onTextChange = { allocationMethod = it }, placeholder = "Allocation method (optional)", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(scheduleId, venueId, courseId, studentId, seatNumber, allocationMethod.ifBlank { null }) },
                enabled = scheduleId.isNotBlank() && venueId.isNotBlank() && courseId.isNotBlank() &&
                    studentId.isNotBlank() && seatNumber.isNotBlank()
            ) { Text("Allocate") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun UpdateSeatingPlanDialog(
    plan: SeatingPlan,
    onDismiss: () -> Unit,
    onSubmit: (seatNumber: String?, venueId: String?) -> Unit
) {
    val spacing = LocalSpacing.current
    var seatNumber by remember { mutableStateOf(plan.seatNumber.orEmpty()) }
    var venueId by remember { mutableStateOf(plan.venueId.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Update seating", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                PremiumTextField(text = seatNumber, onTextChange = { seatNumber = it }, placeholder = "Seat number", imeAction = ImeAction.Next)
                PremiumTextField(text = venueId, onTextChange = { venueId = it }, placeholder = "Venue ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(seatNumber.ifBlank { null }, venueId.ifBlank { null }) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
