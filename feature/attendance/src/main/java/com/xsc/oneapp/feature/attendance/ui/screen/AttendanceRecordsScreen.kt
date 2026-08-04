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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceRecord
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceSession
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

    LaunchedEffect(Unit) { viewModel.loadRecords() }

    AttendanceScaffold(title = "Records", onBack = onBack) { padding ->
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
                ) { RecordRow(it) }
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

@Composable
private fun RecordRow(record: AttendanceRecord) {
    val status = record.attendanceStatusId
    val remarks = record.remarks

    AttendanceCard {
        AttendanceListRow(
            icon = Icons.AutoMirrored.Filled.Assignment,
            title = "Student ${record.studentId ?: "—"}",
            subtitle = record.markedAt,
            trailing = if (status != null) {
                { StatusPill("Status $status", tint = MaterialTheme.colorScheme.primary) }
            } else null
        )
        if (remarks != null) {
            DetailText(remarks, modifier = Modifier.padding(top = 10.dp))
        }
    }
}
