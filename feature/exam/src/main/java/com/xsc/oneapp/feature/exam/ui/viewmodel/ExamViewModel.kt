package com.xsc.oneapp.feature.exam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.core.result.SectionLoader
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.core.result.dataOrNull
import com.xsc.oneapp.core.result.errorMessageOrNull
import com.xsc.oneapp.feature.exam.domain.model.ExamResult
import com.xsc.oneapp.feature.exam.domain.model.ExamSchedule
import com.xsc.oneapp.feature.exam.domain.usecase.GetChallengeRevaluationsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetExamResultsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetExamSchedulesUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetRevaluationRequestsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Summary shown on the Examinations overview, derived from sections the screen already
 * needs rather than fetched separately - same shape as AttendanceOverviewUiState.
 */
data class ExamOverviewUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val scheduleCount: Int = 0,
    val nextSchedule: ExamSchedule? = null,
    val latestResult: ExamResult? = null,
    val revaluationCount: Int = 0
)

/**
 * One ViewModel for the whole Exam graph (Overview, Schedules, Results, Revaluation),
 * shared across those destinations by scoping `hiltViewModel()` to the graph's
 * back-stack entry - see AttendanceViewModel for the pattern this follows, which keeps
 * a section loaded as the student moves between the graph's screens instead of
 * refetching on every hop.
 *
 * Hall Ticket is deliberately excluded: it is a per-schedule drill-in that takes a
 * `scheduleId` route argument, so it keeps its own [HallTicketViewModel] rather than
 * joining this shared instance.
 *
 * Each data set is an independent [SectionLoader], loaded on first use rather than all
 * at once from `init {}` - the previous single-purpose ExamViewModel only fetched
 * schedules; results and revaluation activity were modelled in the domain layer but had
 * no screen at all.
 */
@HiltViewModel
class ExamViewModel @Inject constructor(
    getExamSchedulesUseCase: GetExamSchedulesUseCase,
    getExamResultsUseCase: GetExamResultsUseCase,
    getRevaluationRequestsUseCase: GetRevaluationRequestsUseCase,
    getChallengeRevaluationsUseCase: GetChallengeRevaluationsUseCase
) : ViewModel() {

    val schedules = SectionLoader(viewModelScope) { getExamSchedulesUseCase() }
    val results = SectionLoader(viewModelScope) { getExamResultsUseCase() }
    val revaluationRequests = SectionLoader(viewModelScope) { getRevaluationRequestsUseCase() }
    val challengeRevaluations = SectionLoader(viewModelScope) { getChallengeRevaluationsUseCase() }

    /**
     * Eagerly shared so the value is settled the moment the overview composes, and so
     * unit tests can read it without attaching a collector.
     */
    val overview: StateFlow<ExamOverviewUiState> =
        combine(
            schedules.state,
            results.state,
            revaluationRequests.state
        ) { schedulesState, resultsState, revaluationState ->
            val scheduleRows = schedulesState.dataOrNull().orEmpty()
            ExamOverviewUiState(
                isLoading = schedulesState is UiState.Loading,
                // Schedules is the one call whose failure leaves nothing else to show -
                // course names and dates on Results and Revaluation are read from here
                // too. A failing results or revaluation call degrades its own tile
                // instead of replacing the whole screen with an error the user can do
                // nothing about.
                errorMessage = schedulesState.errorMessageOrNull(),
                scheduleCount = scheduleRows.size,
                nextSchedule = scheduleRows
                    .filter { it.status?.lowercase() == "published" }
                    .minByOrNull { it.fromDate.orEmpty() }
                    ?: scheduleRows.firstOrNull(),
                latestResult = resultsState.dataOrNull().orEmpty()
                    .sortedByDescending { it.createdAt.orEmpty() }
                    .firstOrNull(),
                revaluationCount = revaluationState.dataOrNull().orEmpty().size
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, ExamOverviewUiState())

    fun loadOverview() {
        schedules.loadOnce()
        results.loadOnce()
        revaluationRequests.loadOnce()
    }

    fun refreshOverview() {
        schedules.reload()
        results.reload()
        revaluationRequests.reload()
    }

    fun loadSchedules() = schedules.loadOnce()

    fun loadResults() = results.loadOnce()

    /** Requests and challenges are two stages of one "dispute my mark" story, shown as
     * tabs on one screen (see ExamRevaluationScreen) - both load together rather than
     * per-tab-selection, matching Attendance's Requests screen. */
    fun loadRevaluation() {
        revaluationRequests.loadOnce()
        challengeRevaluations.loadOnce()
    }
}
