package com.xsc.oneapp.feature.attendance.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceRecord
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceSession
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceType
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.attendance.ui.components.AttendanceCard
import com.xsc.oneapp.feature.attendance.ui.components.AttendanceListRow
import com.xsc.oneapp.feature.attendance.ui.components.SectionList
import com.xsc.oneapp.feature.attendance.ui.components.StatusPill
import com.xsc.sdk.commonui.record.DetailText
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.SectionChips
import com.xsc.oneapp.feature.attendance.ui.viewmodel.AttendanceViewModel
import com.xsc.sdk.theme.OneAppSuccess
import com.xsc.sdk.theme.OneAppWarning

/**
 * Everything that answers "what happened, and was it recorded?".
 *
 * Previously three separate top-level tabs (Sessions, Submissions, Records) sitting
 * beside five unrelated ones. Sessions and Submissions are literally the same backend
 * table read twice - `attendanceSession/view` and `attendanceSubmission/view` return
 * an identical row shape and share one domain model - so presenting them as peer
 * destinations invited the reader to look for a difference that does not exist. They
 * are now two views of one screen, framed by what the user is asking.
 */
private val TABS = listOf("Sessions", "Submissions", "Markings")

@Composable
fun AttendanceRecordsScreen(
    onBack: () -> Unit,
    viewModel: AttendanceViewModel,
) {
    var selected by rememberSaveable { mutableIntStateOf(0) }

    val sessionsState by viewModel.sessions.state.collectAsStateWithLifecycle()
    val submissionsState by viewModel.submissions.state.collectAsStateWithLifecycle()
    val recordsState by viewModel.records.state.collectAsStateWithLifecycle()
    val typesState by viewModel.types.state.collectAsStateWithLifecycle()
    val typesById = remember(typesState) {
        (typesState as? UiState.Success)?.data?.associateBy { it.id } ?: emptyMap()
    }

    LaunchedEffect(Unit) { viewModel.loadRecords() }

    RecordScaffold(title = "Records", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SectionChips(options = TABS, selectedIndex = selected, onSelect = { selected = it })

            when (selected) {
                0 -> SectionList(
                    state = sessionsState,
                    emptyMessage = "No attendance sessions have been created yet.",
                    onRetry = viewModel.sessions::reload,
                    key = { it.id ?: it.hashCode() }
                ) { SessionRow(it) }

                1 -> SectionList(
                    state = submissionsState,
                    emptyMessage = "No submission compliance data available yet.",
                    onRetry = viewModel.submissions::reload,
                    key = { it.id ?: it.hashCode() }
                ) { SubmissionRow(it) }

                else -> SectionList(
                    state = recordsState,
                    emptyMessage = "No individual attendance markings recorded yet.",
                    onRetry = viewModel.records::reload,
                    key = { it.id ?: it.hashCode() }
                ) { RecordRow(it, typesById) }
            }
        }
    }
}

@Composable
private fun SessionRow(session: AttendanceSession) {
    val status = session.statusId
    val startedAt = session.startedAt

    AttendanceCard {
        AttendanceListRow(
            icon = Icons.AutoMirrored.Filled.EventNote,
            title = session.remarks?.takeIf { it.isNotBlank() }
                ?: "Session ${session.classSessionId ?: "—"}",
            subtitle = session.sessionDate,
            trailing = if (status != null) {
                { StatusPill("Status $status", tint = MaterialTheme.colorScheme.primary) }
            } else null
        )
        if (startedAt != null) {
            DetailText("Started $startedAt", modifier = Modifier.padding(top = 10.dp))
        }
    }
}

@Composable
private fun SubmissionRow(submission: AttendanceSession) {
    val submitted = submission.submittedAt != null
    AttendanceCard {
        AttendanceListRow(
            icon = Icons.AutoMirrored.Filled.FactCheck,
            title = "Session ${submission.classSessionId ?: "—"}",
            subtitle = submission.sessionDate,
            iconTint = if (submitted) OneAppSuccess else OneAppWarning,
            trailing = {
                StatusPill(
                    if (submitted) "Submitted" else "Pending",
                    tint = if (submitted) OneAppSuccess else OneAppWarning
                )
            }
        )
        val submittedAt = submission.submittedAt
        if (submittedAt != null) {
            DetailText("Submitted $submittedAt", modifier = Modifier.padding(top = 10.dp))
        }
        val remarks = submission.remarks
        if (remarks != null) {
            DetailText(remarks, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

/**
 * [typesById] resolves attendanceStatusId to a real label (Present/Absent/Excused/...)
 * instead of a raw number - AttendanceType is sch_lookup.tb_lookup cat_id=70, "the
 * institution's custom attendance marking codes" per its own doc comment, which is
 * exactly what attendanceStatusId is a foreign key into. Falls back to the raw id only
 * if the type list hasn't loaded or doesn't contain it, same fail-open-while-unresolved
 * shape used elsewhere in this app.
 *
 * Also drops the previous "Student $studentId" line - this screen only ever shows the
 * signed-in student's own records, so naming them by id was both meaningless (a raw
 * number, not a resolved name) and redundant (they already know whose record this is).
 */
@Composable
private fun RecordRow(record: AttendanceRecord, typesById: Map<String?, AttendanceType>) {
    val statusLabel = record.attendanceStatusId?.let { id ->
        typesById[id]?.lookupName ?: "Status $id"
    }
    val remarks = record.remarks

    AttendanceCard {
        AttendanceListRow(
            icon = Icons.AutoMirrored.Filled.Assignment,
            title = record.markedAt ?: "Marking",
            trailing = if (statusLabel != null) {
                { StatusPill(statusLabel, tint = MaterialTheme.colorScheme.primary) }
            } else null
        )
        if (remarks != null) {
            DetailText(remarks, modifier = Modifier.padding(top = 10.dp))
        }
    }
}
