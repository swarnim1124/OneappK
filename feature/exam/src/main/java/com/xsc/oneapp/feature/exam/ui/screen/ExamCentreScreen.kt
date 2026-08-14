@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.exam.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Domain
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
import com.xsc.oneapp.feature.exam.domain.model.ExamCentre
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

/** m_exam §3.1 sm_examCenter/examCentre - full CRUD list for exam-centre registration
 * and venue capacity. Admin-only, matching [ExamCentre]'s own kdoc: the contract has no
 * student-facing view of this list. */
@Composable
fun ExamCentreScreen(onBack: () -> Unit, viewModel: ExamAdminViewModel) {
    val state by viewModel.examCentres.state.collectAsStateWithLifecycle()
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCentre by remember { mutableStateOf<ExamCentre?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadExamCentres() }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is ExamEffect.ShowToast) {
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
            }
        }
    }

    RecordScaffold(
        title = "Exam centres",
        onBack = onBack,
        floatingActionButton = {
            if (permissions.canAddExamCentre) {
                ExtendedFloatingActionButton(
                    text = { Text("Add centre") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = { showAddDialog = true }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            ExamSectionList(
                state = state,
                emptyMessage = "No exam centres registered yet.",
                onRetry = viewModel.examCentres::reload,
                key = { it.id ?: it.hashCode() }
            ) { centre ->
                ResponsiveContent {
                    ExamCentreCard(
                        centre,
                        canUpdate = permissions.canUpdateExamCentre,
                        canDelete = permissions.canDeleteExamCentre,
                        onUpdate = { editingCentre = centre },
                        onDelete = { centre.id?.let(viewModel::deleteExamCentre) }
                    )
                }
            }
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.padding(padding)) { data ->
            Snackbar(snackbarData = data)
        }
    }

    if (showAddDialog) {
        ExamCentreDialog(
            title = "Add exam centre",
            onDismiss = { showAddDialog = false },
            onSubmit = { centreName, capacity, address, courses, _ ->
                viewModel.addExamCentre(centreName, capacity, address, courses)
                showAddDialog = false
            }
        )
    }

    editingCentre?.let { centre ->
        ExamCentreDialog(
            title = "Update exam centre",
            initialCentreName = centre.centreName.orEmpty(),
            initialCapacity = centre.capacity.orEmpty(),
            initialAddress = centre.address.orEmpty(),
            initialCourses = centre.courses.joinToString(", "),
            initialStatus = centre.status.orEmpty(),
            showStatus = true,
            onDismiss = { editingCentre = null },
            onSubmit = { centreName, capacity, address, courses, status ->
                centre.id?.let { viewModel.updateExamCentre(it, centreName, capacity, address, courses, status) }
                editingCentre = null
            }
        )
    }
}

@Composable
private fun ExamCentreCard(
    centre: ExamCentre,
    canUpdate: Boolean,
    canDelete: Boolean,
    onUpdate: () -> Unit,
    onDelete: () -> Unit
) {
    val spacing = LocalSpacing.current

    RecordCard {
        RecordRow(
            icon = Icons.Default.Domain,
            title = centre.centreName ?: "Exam centre",
            subtitle = centre.address,
            trailing = centre.status?.let { status -> { StatusPill(status) } }
        )
        centre.capacity?.let {
            Text(
                "Capacity: $it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = spacing.sm)
            )
        }
        if (centre.courses.isNotEmpty()) {
            Text(
                "Courses: ${centre.courses.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = spacing.xs)
            )
        }
        AdminRowActions(canUpdate = canUpdate, canDelete = canDelete, onUpdate = onUpdate, onDelete = onDelete)
    }
}

/** Shared by both add and update - [showStatus] is false for add since a freshly
 * created centre has no status to set yet, matching how [ExamAdminViewModel.addExamCentre]
 * itself takes no status parameter. */
@Composable
private fun ExamCentreDialog(
    title: String,
    onDismiss: () -> Unit,
    onSubmit: (centreName: String, capacity: String, address: String?, courses: List<String>, status: String?) -> Unit,
    initialCentreName: String = "",
    initialCapacity: String = "",
    initialAddress: String = "",
    initialCourses: String = "",
    initialStatus: String = "",
    showStatus: Boolean = false
) {
    val spacing = LocalSpacing.current
    var centreName by remember { mutableStateOf(initialCentreName) }
    var capacity by remember { mutableStateOf(initialCapacity) }
    var address by remember { mutableStateOf(initialAddress) }
    var coursesText by remember { mutableStateOf(initialCourses) }
    var status by remember { mutableStateOf(initialStatus) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                PremiumTextField(text = centreName, onTextChange = { centreName = it }, placeholder = "Centre name", imeAction = ImeAction.Next)
                PremiumTextField(text = capacity, onTextChange = { capacity = it }, placeholder = "Capacity", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = address, onTextChange = { address = it }, placeholder = "Address (optional)", imeAction = ImeAction.Next)
                PremiumTextField(
                    text = coursesText,
                    onTextChange = { coursesText = it },
                    placeholder = "Course IDs, comma-separated",
                    imeAction = if (showStatus) ImeAction.Next else ImeAction.Done
                )
                if (showStatus) {
                    PremiumTextField(text = status, onTextChange = { status = it }, placeholder = "Status", imeAction = ImeAction.Done)
                }
            }
        },
        confirmButton = {
            val courses = coursesText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            TextButton(
                onClick = {
                    onSubmit(centreName, capacity, address.ifBlank { null }, courses, if (showStatus) status.ifBlank { null } else null)
                },
                enabled = centreName.isNotBlank() && capacity.isNotBlank()
            ) { Text(if (showStatus) "Save" else "Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
