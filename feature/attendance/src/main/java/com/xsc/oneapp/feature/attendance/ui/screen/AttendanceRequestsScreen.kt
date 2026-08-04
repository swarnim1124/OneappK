package com.xsc.oneapp.feature.attendance.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceCorrectionRequest
import com.xsc.oneapp.feature.attendance.ui.components.AttendanceCard
import com.xsc.oneapp.feature.attendance.ui.components.AttendanceListRow
import com.xsc.oneapp.feature.attendance.ui.components.AttendanceScaffold
import com.xsc.oneapp.feature.attendance.ui.components.DetailText
import com.xsc.oneapp.feature.attendance.ui.components.SectionChips
import com.xsc.oneapp.feature.attendance.ui.components.SectionList
import com.xsc.oneapp.feature.attendance.ui.components.StatusPill
import com.xsc.oneapp.feature.attendance.ui.viewmodel.AttendanceViewModel
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

    val exceptionsState by viewModel.exceptions.state.collectAsStateWithLifecycle()
    val condonationsState by viewModel.condonations.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadRequests() }

    AttendanceScaffold(title = "Requests", onBack = onBack) { padding ->
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
