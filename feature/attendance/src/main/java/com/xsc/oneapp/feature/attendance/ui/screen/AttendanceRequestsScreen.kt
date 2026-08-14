@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.attendance.ui.screen

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
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceCorrectionRequest
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceSession
import com.xsc.oneapp.feature.attendance.ui.components.AttendanceCard
import com.xsc.oneapp.feature.attendance.ui.components.AttendanceListRow
import com.xsc.oneapp.feature.attendance.ui.components.SectionList
import com.xsc.oneapp.feature.attendance.ui.components.StatusPill
import com.xsc.oneapp.feature.attendance.ui.viewmodel.AttendanceViewModel
import com.xsc.sdk.commonui.record.DetailText
import com.xsc.sdk.commonui.record.NotYetAvailableScreen
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.SectionChips
import com.xsc.sdk.theme.OneAppSuccess
import com.xsc.sdk.theme.OneAppWarning

/**
 * Correction workflow in one place: exceptions raised against a marking, and
 * condonations granted against a shortage.
 *
 * They were adjacent top-level tabs before, which is misleading twice over - they read
 * the same `tb_att_correction` table with an identical response shape (one domain model
 * backs both), and they are two stages of a single "my attendance is wrong / excuse it"
 * story rather than two separate features.
 */
private val TABS = listOf("Exceptions", "Condonations")

@Composable
fun AttendanceRequestsScreen(
    onBack: () -> Unit,
    viewModel: AttendanceViewModel
) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var showRequestForm by remember { mutableStateOf(false) }

    val exceptionsState by viewModel.exceptions.state.collectAsStateWithLifecycle()
    val condonationsState by viewModel.condonations.state.collectAsStateWithLifecycle()
    val sessionsState by viewModel.sessions.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadRequests() }

    if (showRequestForm) {
        RequestCorrectionScreen(
            sessionsState = sessionsState,
            onBack = { showRequestForm = false }
        )
        return
    }

    RecordScaffold(
        title = "Requests",
        onBack = onBack,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("New request") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = { showRequestForm = true }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SectionChips(options = TABS, selectedIndex = selected, onSelect = { selected = it })

            if (selected == 0) {
                SectionList(
                    state = exceptionsState,
                    emptyMessage = "No correction requests have been raised.",
                    onRetry = viewModel.exceptions::reload,
                    key = { it.id ?: it.hashCode() }
                ) { CorrectionRow(it, Icons.Default.ReportProblem) }
            } else {
                SectionList(
                    state = condonationsState,
                    emptyMessage = "No condonation requests on record.",
                    onRetry = viewModel.condonations::reload,
                    key = { it.id ?: it.hashCode() }
                ) { CorrectionRow(it, Icons.Default.HealthAndSafety) }
            }
        }
    }
}

/**
 * One row for both tabs, since both render the same model. Approval state is expressed
 * as a pill rather than a raw `status_id`, which is what the old screen printed
 * ("Status 708") - an internal lookup id no user can interpret. The id is still shown
 * as a secondary detail so nothing is lost for staff who do read them.
 */
@Composable
private fun CorrectionRow(
    request: AttendanceCorrectionRequest,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val approved = request.approvedAt != null
    val pillColor: Color = if (approved) OneAppSuccess else OneAppWarning

    AttendanceCard {
        AttendanceListRow(
            icon = icon,
            title = "Record ${request.attendanceRecordId ?: "—"}",
            subtitle = request.requestedAt?.let { "Requested $it" },
            iconTint = MaterialTheme.colorScheme.tertiary,
            trailing = { StatusPill(if (approved) "Approved" else "Pending", tint = pillColor) }
        )

        val remarks = request.remarks
        if (remarks != null) {
            DetailText(remarks, modifier = Modifier.padding(top = 10.dp))
        }
        val approvedAt = request.approvedAt
        if (approvedAt != null) {
            DetailText("Approved $approvedAt", modifier = Modifier.padding(top = 4.dp))
        }
        val statusId = request.statusId
        if (statusId != null) {
            DetailText("Status code $statusId", modifier = Modifier.padding(top = 4.dp))
        }
    }
}

/**
 * Matches request_correction/code.html: an intro line, a session picker, and a reason
 * field, ending at an honest "not available yet" terminal state on submit - same
 * pattern as Fee's PayFeesReviewScreen -> PaymentNotYetAvailableScreen. There is no
 * add/submit use case for attendance corrections anywhere in this module's domain
 * layer (only eight read-only "Get" use cases exist), so this can't actually create a
 * request yet. The mockup's "Attach Proof (Optional)" section is omitted entirely: no
 * file/media SDK exists in this project to back an upload.
 */
@Composable
private fun RequestCorrectionScreen(
    sessionsState: UiState<List<AttendanceSession>>,
    onBack: () -> Unit
) {
    var showSubmitted by remember { mutableStateOf(false) }

    if (showSubmitted) {
        CorrectionNotYetAvailableScreen(onBack = { showSubmitted = false })
        return
    }

    RecordScaffold(title = "New Correction Request", onBack = onBack) { padding ->
        var selectedSession by remember { mutableStateOf<AttendanceSession?>(null) }
        var reason by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Please provide details regarding the attendance session you believe was " +
                    "recorded incorrectly. Your request will be reviewed by the course instructor.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AttendanceCard {
                Text(
                    "Session Details",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                SessionPicker(
                    sessionsState = sessionsState,
                    selected = selectedSession,
                    onSelect = { selectedSession = it },
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            AttendanceCard {
                Text(
                    "Correction Details",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for correction") },
                    placeholder = { Text("e.g., Was present but marked absent") },
                    singleLine = false,
                    minLines = 3,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick = { showSubmitted = true },
                    enabled = selectedSession != null && reason.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Submit Request")
                }
            }
        }
    }
}

@Composable
private fun SessionPicker(
    sessionsState: UiState<List<AttendanceSession>>,
    selected: AttendanceSession?,
    onSelect: (AttendanceSession) -> Unit,
    modifier: Modifier = Modifier
) {
    val sessions = (sessionsState as? UiState.Success)?.data.orEmpty()
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it && sessions.isNotEmpty() },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected?.let(::sessionLabel).orEmpty(),
            onValueChange = {},
            readOnly = true,
            enabled = sessions.isNotEmpty(),
            label = { Text("Select session") },
            placeholder = {
                Text(if (sessions.isEmpty()) "No recent sessions available" else "Choose a recent class session")
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = MaterialTheme.shapes.small,
            colors = OutlinedTextFieldDefaults.colors(),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, sessions.isNotEmpty())
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            sessions.forEach { session ->
                DropdownMenuItem(
                    text = { Text(sessionLabel(session)) },
                    onClick = {
                        onSelect(session)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun sessionLabel(session: AttendanceSession): String =
    session.remarks?.takeIf { it.isNotBlank() }
        ?: listOfNotNull(
            session.classSessionId?.let { "Session $it" },
            session.sessionDate
        ).joinToString(" · ").ifBlank { "Session ${session.id ?: "—"}" }

/**
 * Honest terminal state, mirroring Fee's PaymentNotYetAvailableScreen: there's no
 * submit-correction use case in this module's domain layer, so a "Request submitted"
 * confirmation here would tell a student their correction was filed when it wasn't.
 */
@Composable
private fun CorrectionNotYetAvailableScreen(onBack: () -> Unit) {
    NotYetAvailableScreen(
        title = "New Correction Request",
        icon = Icons.Default.EventAvailable,
        headline = "Correction requests aren't available yet",
        message = "This institution hasn't enabled in-app correction requests yet. Please " +
            "contact your course instructor directly about this attendance record.",
        onBack = onBack
    )
}
