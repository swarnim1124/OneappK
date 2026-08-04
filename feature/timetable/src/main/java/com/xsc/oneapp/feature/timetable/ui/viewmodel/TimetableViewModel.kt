package com.xsc.oneapp.feature.timetable.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.core.result.SectionLoader
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.timetable.domain.model.AcademicCalendar
import com.xsc.oneapp.feature.timetable.domain.model.FacultyAllocation
import com.xsc.oneapp.feature.timetable.domain.model.RoomAllocation
import com.xsc.oneapp.feature.timetable.domain.model.Substitution
import com.xsc.oneapp.feature.timetable.domain.model.TimeSlot
import com.xsc.oneapp.feature.timetable.domain.model.TimetableApproval
import com.xsc.oneapp.feature.timetable.domain.model.TimetableEntry
import com.xsc.oneapp.feature.timetable.domain.model.WorkingDay
import com.xsc.oneapp.feature.timetable.domain.usecase.GetAcademicCalendarUseCase
import com.xsc.oneapp.feature.timetable.domain.usecase.GetFacultyAllocationsUseCase
import com.xsc.oneapp.feature.timetable.domain.usecase.GetRoomAllocationsUseCase
import com.xsc.oneapp.feature.timetable.domain.usecase.GetSubstitutionsUseCase
import com.xsc.oneapp.feature.timetable.domain.usecase.GetTimeSlotsUseCase
import com.xsc.oneapp.feature.timetable.domain.usecase.GetTimetableApprovalsUseCase
import com.xsc.oneapp.feature.timetable.domain.usecase.GetTimetableEntriesUseCase
import com.xsc.oneapp.feature.timetable.domain.usecase.GetWorkingDaysUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * One [SectionLoader] per timetable sub-module.
 *
 * The previous version fired all eight use cases from `init {}` - eight concurrent
 * requests at the single `POST /` dispatcher on every entry, for seven tabs the user
 * could not see, refetched on every revisit.
 *
 * A section now loads the first time its tab is selected, via [onTabSelected]. Public
 * names are unchanged, so the screen's retry wiring is untouched.
 */
@HiltViewModel
class TimetableViewModel @Inject constructor(
    getTimetableEntriesUseCase: GetTimetableEntriesUseCase,
    getWorkingDaysUseCase: GetWorkingDaysUseCase,
    getTimeSlotsUseCase: GetTimeSlotsUseCase,
    getAcademicCalendarUseCase: GetAcademicCalendarUseCase,
    getFacultyAllocationsUseCase: GetFacultyAllocationsUseCase,
    getRoomAllocationsUseCase: GetRoomAllocationsUseCase,
    getSubstitutionsUseCase: GetSubstitutionsUseCase,
    getTimetableApprovalsUseCase: GetTimetableApprovalsUseCase
) : ViewModel() {

    private val entries = SectionLoader(viewModelScope) { getTimetableEntriesUseCase() }
    private val workingDays = SectionLoader(viewModelScope) { getWorkingDaysUseCase() }
    private val timeSlots = SectionLoader(viewModelScope) { getTimeSlotsUseCase() }
    private val academicCalendar = SectionLoader(viewModelScope) { getAcademicCalendarUseCase() }
    private val facultyAllocations = SectionLoader(viewModelScope) { getFacultyAllocationsUseCase() }
    private val roomAllocations = SectionLoader(viewModelScope) { getRoomAllocationsUseCase() }
    private val substitutions = SectionLoader(viewModelScope) { getSubstitutionsUseCase() }
    private val approvals = SectionLoader(viewModelScope) { getTimetableApprovalsUseCase() }

    val entriesState: StateFlow<UiState<List<TimetableEntry>>> = entries.state
    val workingDaysState: StateFlow<UiState<List<WorkingDay>>> = workingDays.state
    val timeSlotsState: StateFlow<UiState<List<TimeSlot>>> = timeSlots.state
    val academicCalendarState: StateFlow<UiState<AcademicCalendar?>> = academicCalendar.state
    val facultyAllocationsState: StateFlow<UiState<List<FacultyAllocation>>> = facultyAllocations.state
    val roomAllocationsState: StateFlow<UiState<List<RoomAllocation>>> = roomAllocations.state
    val substitutionsState: StateFlow<UiState<List<Substitution>>> = substitutions.state
    val approvalsState: StateFlow<UiState<List<TimetableApproval>>> = approvals.state

    /**
     * Loads the selected tab's data if it hasn't been fetched yet. Indices match
     * TimetableScreen's TAB_TITLES order.
     *
     * Tab 0 needs two sections: the schedule renders each entry's period using the time
     * slot list, so both are requested together - two calls rather than eight.
     */
    fun onTabSelected(index: Int) {
        when (index) {
            0 -> {
                entries.loadOnce()
                timeSlots.loadOnce()
            }
            1 -> workingDays.loadOnce()
            2 -> timeSlots.loadOnce()
            3 -> academicCalendar.loadOnce()
            4 -> facultyAllocations.loadOnce()
            5 -> roomAllocations.loadOnce()
            6 -> substitutions.loadOnce()
            7 -> approvals.loadOnce()
        }
    }

    // Explicit user-driven refresh, wired to each tab's retry button. Names unchanged.
    fun loadEntries() = entries.reload()
    fun loadWorkingDays() = workingDays.reload()
    fun loadTimeSlots() = timeSlots.reload()
    fun loadAcademicCalendar() = academicCalendar.reload()
    fun loadFacultyAllocations() = facultyAllocations.reload()
    fun loadRoomAllocations() = roomAllocations.reload()
    fun loadSubstitutions() = substitutions.reload()
    fun loadApprovals() = approvals.reload()
}
