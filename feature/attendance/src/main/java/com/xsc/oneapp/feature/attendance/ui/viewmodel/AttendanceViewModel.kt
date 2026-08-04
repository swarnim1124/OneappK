package com.xsc.oneapp.feature.attendance.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.core.result.SectionLoader
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.core.result.dataOrNull
import com.xsc.oneapp.core.result.errorMessageOrNull
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceSession
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceShortage
import com.xsc.oneapp.feature.attendance.domain.usecase.GetAttendanceConfigurationsUseCase
import com.xsc.oneapp.feature.attendance.domain.usecase.GetAttendanceExceptionsUseCase
import com.xsc.oneapp.feature.attendance.domain.usecase.GetAttendanceRecordsUseCase
import com.xsc.oneapp.feature.attendance.domain.usecase.GetAttendanceSessionsUseCase
import com.xsc.oneapp.feature.attendance.domain.usecase.GetAttendanceShortageUseCase
import com.xsc.oneapp.feature.attendance.domain.usecase.GetAttendanceTypesUseCase
import com.xsc.oneapp.feature.attendance.domain.usecase.GetCondonationsUseCase
import com.xsc.oneapp.feature.attendance.domain.usecase.GetSubmissionComplianceReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Summary shown on the Attendance overview, derived from sections the screen already
 * needs rather than fetched separately.
 */
data class AttendanceOverviewUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val shortage: AttendanceShortage? = null,
    val pendingSubmissions: Int = 0,
    val openRequests: Int = 0,
    val recentSessions: List<AttendanceSession> = emptyList(),
)

/**
 * One ViewModel for the whole Attendance graph, shared across its destinations by
 * scoping `hiltViewModel()` to the graph's back-stack entry (see AttendanceNavigation).
 * That keeps a section loaded as the user moves between Overview, Records and Requests
 * instead of refetching on every hop.
 *
 * Each data set is a [SectionLoader]: independent state, independent retry, loaded on
 * first use. The previous version declared eight MutableStateFlows plus eight
 * near-identical `loadX()` methods and fired all eight from `init {}` - eight parallel
 * requests to the same dispatcher endpoint on every entry, for seven tabs the user
 * could not see.
 */
@HiltViewModel
class AttendanceViewModel @Inject constructor(
    getAttendanceShortage: GetAttendanceShortageUseCase,
    getAttendanceConfigurations: GetAttendanceConfigurationsUseCase,
    getAttendanceTypes: GetAttendanceTypesUseCase,
    getAttendanceSessions: GetAttendanceSessionsUseCase,
    getSubmissionComplianceReport: GetSubmissionComplianceReportUseCase,
    getAttendanceRecords: GetAttendanceRecordsUseCase,
    getAttendanceExceptions: GetAttendanceExceptionsUseCase,
    getCondonations: GetCondonationsUseCase
) : ViewModel() {

    val shortage = SectionLoader(viewModelScope) { getAttendanceShortage() }
    val sessions = SectionLoader(viewModelScope) { getAttendanceSessions() }
    val submissions = SectionLoader(viewModelScope) { getSubmissionComplianceReport() }
    val records = SectionLoader(viewModelScope) { getAttendanceRecords() }
    val exceptions = SectionLoader(viewModelScope) { getAttendanceExceptions() }
    val condonations = SectionLoader(viewModelScope) { getCondonations() }
    val configurations = SectionLoader(viewModelScope) { getAttendanceConfigurations() }
    val types = SectionLoader(viewModelScope) { getAttendanceTypes() }

    /**
     * Eagerly shared so the value is settled the moment the overview composes, and so
     * unit tests can read it without attaching a collector.
     */
    val overview: StateFlow<AttendanceOverviewUiState> =
        combine(
            shortage.state,
            submissions.state,
            exceptions.state
        ) { shortageState, submissionsState, exceptionsState ->
            val submissionRows = submissionsState.dataOrNull().orEmpty()
            AttendanceOverviewUiState(
                isLoading = (shortageState is UiState.Loading ||
                    submissionsState is UiState.Loading ||
                    exceptionsState is UiState.Loading),
                // Only the shortage failure becomes the screen's error: it backs the
                // headline card. A failing submissions or requests call degrades those
                // tiles to zero instead of replacing the whole screen with an error the
                // user can do nothing about.
                errorMessage = shortageState.errorMessageOrNull(),
                shortage = shortageState.dataOrNull()?.firstOrNull(),
                pendingSubmissions = submissionRows.count { it.submittedAt == null },
                openRequests = exceptionsState.dataOrNull().orEmpty().count { it.approvedAt == null },
                recentSessions = submissionRows
                    .sortedByDescending { it.sessionDate.orEmpty() }
                    .take(MAX_RECENT_SESSIONS)
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, AttendanceOverviewUiState())

    fun loadOverview() {
        shortage.loadOnce()
        submissions.loadOnce()
        exceptions.loadOnce()
    }

    fun refreshOverview() {
        shortage.reload()
        submissions.reload()
        exceptions.reload()
    }

    fun loadRecords() {
        sessions.loadOnce()
        submissions.loadOnce()
        records.loadOnce()
    }

    fun loadRequests() {
        exceptions.loadOnce()
        condonations.loadOnce()
    }

    fun loadPolicy() {
        configurations.loadOnce()
        types.loadOnce()
    }

    private companion object {
        const val MAX_RECENT_SESSIONS = 3
    }
}
