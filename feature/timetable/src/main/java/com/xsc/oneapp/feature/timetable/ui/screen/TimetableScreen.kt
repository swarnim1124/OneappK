@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.timetable.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.timetable.domain.model.AcademicCalendar
import com.xsc.oneapp.feature.timetable.domain.model.FacultyAllocation
import com.xsc.oneapp.feature.timetable.domain.model.RoomAllocation
import com.xsc.oneapp.feature.timetable.domain.model.Substitution
import com.xsc.oneapp.feature.timetable.domain.model.TimeSlot
import com.xsc.oneapp.feature.timetable.domain.model.TimetableApproval
import com.xsc.oneapp.feature.timetable.domain.model.TimetableEntry
import com.xsc.oneapp.feature.timetable.domain.model.WorkingDay
import com.xsc.oneapp.feature.timetable.ui.viewmodel.TimetableViewModel
import java.time.LocalDate
import com.xsc.sdk.commonui.record.EmptyState
import com.xsc.sdk.commonui.record.ErrorState
import com.xsc.sdk.commonui.record.IconBadge
import com.xsc.sdk.commonui.record.LoadingState
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.SectionChips
import com.xsc.sdk.commonui.record.StatusPill
// Aliased so the eight tab bodies below keep calling `TimetableCard { ... }` unchanged -
// the implementation is now the shared record card rather than a private copy.
import com.xsc.sdk.commonui.record.RecordCard as TimetableCard

private val TAB_TITLES = listOf("Schedule", "Working Days", "Time Slots", "Calendar", "Faculty", "Rooms", "Substitutions", "Approvals")

/**
 * Restyled only. Every tab body, the today-highlighting logic, all ViewModel calls and
 * retry paths are unchanged.
 *
 * The eight-tab ScrollableTabRow left "Substitutions" and "Approvals" permanently
 * off-screen on a phone with no affordance that they existed; scrollable chips make the
 * overflow obvious and draggable.
 */
@Composable
fun TimetableScreen(
    onBack: () -> Unit,
    viewModel: TimetableViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val entriesState by viewModel.entriesState.collectAsStateWithLifecycle()
    val workingDaysState by viewModel.workingDaysState.collectAsStateWithLifecycle()
    val timeSlotsState by viewModel.timeSlotsState.collectAsStateWithLifecycle()
    val academicCalendarState by viewModel.academicCalendarState.collectAsStateWithLifecycle()
    val facultyAllocationsState by viewModel.facultyAllocationsState.collectAsStateWithLifecycle()
    val roomAllocationsState by viewModel.roomAllocationsState.collectAsStateWithLifecycle()
    val substitutionsState by viewModel.substitutionsState.collectAsStateWithLifecycle()
    val approvalsState by viewModel.approvalsState.collectAsStateWithLifecycle()

    // Fetches only the visible tab, and only the first time it is opened. Replaces the
    // ViewModel's old `init {}` block, which requested all eight sub-modules at once.
    LaunchedEffect(selectedTab) { viewModel.onTabSelected(selectedTab) }

    RecordScaffold(title = "Timetable", onBack = onBack) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SectionChips(
                options = TAB_TITLES,
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it }
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> ScheduleTab(entriesState, timeSlotsState, viewModel::loadEntries)
                    1 -> WorkingDaysTab(workingDaysState, viewModel::loadWorkingDays)
                    2 -> TimeSlotsTab(timeSlotsState, viewModel::loadTimeSlots)
                    3 -> CalendarTab(academicCalendarState, viewModel::loadAcademicCalendar)
                    4 -> FacultyTab(facultyAllocationsState, viewModel::loadFacultyAllocations)
                    5 -> RoomsTab(roomAllocationsState, viewModel::loadRoomAllocations)
                    6 -> SubstitutionsTab(substitutionsState, viewModel::loadSubstitutions)
                    7 -> ApprovalsTab(approvalsState, viewModel::loadApprovals)
                }
            }
        }
    }
}

// --- Schedule (TimetableEntry) ---

@Composable
private fun ScheduleTab(
    state: UiState<List<TimetableEntry>>,
    timeSlotsState: UiState<List<TimeSlot>>,
    onRetry: () -> Unit
) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No timetable entries published yet.")
        } else {
            // dayOfWeek comes back as "MONDAY"/"TUESDAY"/... (see the confirmed
            // response shape) which matches java.time.DayOfWeek.name exactly, so
            // today's classes can be surfaced first without any backend filter.
            val today = remember { LocalDate.now().dayOfWeek.name }
            val sortedEntries = remember(state.data, today) {
                state.data.sortedByDescending { it.dayOfWeek == today }
            }
            // Time Slots load independently (its own tab/call) but already carry
            // start/end times - joining them client-side here turns a raw
            // "Slot 2" into "09:00–09:50" without any new network call or
            // backend change, only when that slot has actually loaded.
            val timeSlotsById = remember(timeSlotsState) {
                (timeSlotsState as? UiState.Success)?.data?.associateBy { it.id } ?: emptyMap()
            }
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(sortedEntries, key = { it.hashCode() }) { entry ->
                    val isToday = entry.dayOfWeek == today
                    TimetableCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                IconBadge(Icons.Default.Schedule, tint = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(entry.courseId ?: "Course —", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                        if (isToday) {
                                            StatusPill("TODAY")
                                        }
                                    }
                                    val slotLabel = entry.timeSlotId?.let { id -> timeSlotsById[id]?.displayRange() ?: "Slot $id" }
                                    val dayAndSlot = listOfNotNull(entry.dayOfWeek, slotLabel).joinToString(" · ")
                                    if (dayAndSlot.isNotBlank()) {
                                        Text(dayAndSlot, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            if (entry.ttStatus != null) {
                                StatusPill(entry.ttStatus, tint = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                        val details = listOfNotNull(
                            entry.roomId?.let { "Room $it" },
                            entry.facultyId?.let { "Faculty $it" },
                            entry.sessionTypeId
                        ).joinToString(" · ")
                        if (details.isNotBlank()) {
                            Text(details, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
                        }
                    }
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

/** "09:00:00" -> "09:00" - strips the seconds component the backend always
 * includes but no one displays. */
private fun TimeSlot.displayRange(): String? {
    val start = startTime?.take(5)
    val end = endTime?.take(5)
    return listOfNotNull(start, end).takeIf { it.isNotEmpty() }?.joinToString("–")
}

// --- Working Days ---

@Composable
private fun WorkingDaysTab(state: UiState<List<WorkingDay>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No working day configuration published yet.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(state.data, key = { it.hashCode() }) { day ->
                    TimetableCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                IconBadge(Icons.Default.CalendarMonth)
                                Column {
                                    Text(day.name ?: day.dayName ?: "—", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    if (day.dayName != null) {
                                        Text(day.dayName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            if (day.isWorkingDay == "true") {
                                StatusPill("Working Day")
                            } else if (day.isWorkingDay == "false") {
                                StatusPill("Non-Working", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        if (day.effectiveFrom != null) {
                            val range = day.effectiveTo?.let { "${day.effectiveFrom} – $it" } ?: "From ${day.effectiveFrom}"
                            Text(range, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
                        }
                    }
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

// --- Time Slots ---

@Composable
private fun TimeSlotsTab(state: UiState<List<TimeSlot>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No time slots configured yet.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(state.data, key = { it.hashCode() }) { slot ->
                    TimetableCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                IconBadge(Icons.Default.AccessTime)
                                Column {
                                    Text(slot.slotName ?: "—", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    val range = listOfNotNull(slot.startTime, slot.endTime).joinToString(" – ")
                                    if (range.isNotBlank()) {
                                        Text(range, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            if (slot.isBreak == "true") {
                                StatusPill("Break", tint = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

// --- Academic Calendar (single object, not a list) ---

@Composable
private fun CalendarTab(state: UiState<AcademicCalendar?>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> {
            val calendar = state.data
            if (calendar == null) {
                EmptyState("No academic calendar data available.")
            } else {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    TimetableCard {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconBadge(Icons.Default.Event)
                            Column {
                                Text("Academic Year ${calendar.academicYearId ?: "—"}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                if (calendar.termId != null) {
                                    Text("Term ${calendar.termId}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        if (calendar.note != null) {
                            Text(calendar.note, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

// --- Faculty Allocations ---

@Composable
private fun FacultyTab(state: UiState<List<FacultyAllocation>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No faculty allocations published yet.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(state.data, key = { it.hashCode() }) { allocation ->
                    TimetableCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                IconBadge(Icons.Default.Groups)
                                Column {
                                    Text("Faculty ${allocation.facultyId ?: "—"}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    if (allocation.workloadPercent != null) {
                                        Text("${allocation.workloadPercent}% workload", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            if (allocation.assignmentRoleId != null) {
                                StatusPill(allocation.assignmentRoleId)
                            }
                        }
                        if (allocation.remarks != null) {
                            Text(allocation.remarks, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
                        }
                    }
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

// --- Room Allocations ---

@Composable
private fun RoomsTab(state: UiState<List<RoomAllocation>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No room allocations published yet.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(state.data, key = { it.hashCode() }) { allocation ->
                    TimetableCard {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconBadge(Icons.Default.MeetingRoom)
                            Column {
                                Text("Room ${allocation.roomId ?: "—"}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                val details = listOfNotNull(allocation.dayOfWeek, allocation.timeSlotId?.let { "Slot $it" }).joinToString(" · ")
                                if (details.isNotBlank()) {
                                    Text(details, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

// --- Substitutions ---

@Composable
private fun SubstitutionsTab(state: UiState<List<Substitution>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No substitutions on record.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(state.data, key = { it.hashCode() }) { substitution ->
                    TimetableCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                IconBadge(Icons.Default.SwapHoriz, tint = MaterialTheme.colorScheme.tertiary)
                                Column {
                                    val change = when {
                                        substitution.oldFacultyId != null && substitution.newFacultyId != null ->
                                            "Faculty ${substitution.oldFacultyId} → ${substitution.newFacultyId}"
                                        substitution.oldRoomId != null && substitution.newRoomId != null ->
                                            "Room ${substitution.oldRoomId} → ${substitution.newRoomId}"
                                        else -> substitution.changeTypeId ?: "Substitution"
                                    }
                                    Text(change, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    if (substitution.newSessionDate != null) {
                                        Text(substitution.newSessionDate, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            if (substitution.status != null) {
                                StatusPill(substitution.status)
                            }
                        }
                        if (substitution.reason != null) {
                            Text(substitution.reason, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
                        }
                    }
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

// --- Approvals ---

@Composable
private fun ApprovalsTab(state: UiState<List<TimetableApproval>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No approval requests on record.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(state.data, key = { it.hashCode() }) { approval ->
                    TimetableCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                IconBadge(Icons.Default.CheckCircle, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text(approval.ttCode ?: "—", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    if (approval.description != null) {
                                        Text(approval.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            if (approval.statusId != null) {
                                StatusPill(approval.statusId, tint = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}
