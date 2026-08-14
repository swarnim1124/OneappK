package com.xsc.oneapp.feature.exam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.core.result.SectionLoader
import com.xsc.oneapp.feature.exam.domain.model.ChallengeRevaluation
import com.xsc.oneapp.feature.exam.domain.model.ExamAppeal
import com.xsc.oneapp.feature.exam.domain.model.ExamResult
import com.xsc.oneapp.feature.exam.domain.model.ExamSchedule
import com.xsc.oneapp.feature.exam.domain.model.MalpracticeCase
import com.xsc.oneapp.feature.exam.domain.model.PhotocopyRequest
import com.xsc.oneapp.feature.exam.domain.model.ReappearExam
import com.xsc.oneapp.feature.exam.domain.model.RevaluationRequest
import com.xsc.oneapp.feature.exam.domain.model.SupplementaryExam
import com.xsc.oneapp.feature.exam.domain.usecase.GetChallengeRevaluationsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetExamResultsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetExamSchedulesUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetMyAppealsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetMyMalpracticeCasesUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetMyPhotocopyRequestsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetReappearExamsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetRevaluationRequestsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetSupplementaryExamsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.RegisterForReappearExamUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.RegisterForSupplementaryExamUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.SubmitAppealUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.SubmitChallengeRevaluationUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.SubmitPhotocopyRequestUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.SubmitRevaluationRequestUseCase
import com.xsc.oneapp.feature.exam.ui.state.ExamEffect
import com.xsc.sdk.network.APIError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * One ViewModel for the whole Exam graph, shared across its destinations by scoping
 * `hiltViewModel()` to the graph's back-stack entry (see ExamNavigation) - same
 * precedent as AttendanceViewModel. Every read section is an independent
 * [SectionLoader]: opening Exams no longer fires nine concurrent requests for tabs the
 * student hasn't opened, and revisiting a tab doesn't refetch it.
 *
 * [HallTicketViewModel] is deliberately not folded in here even though it is reached
 * from this graph: it takes a `scheduleId` nav arg and is meaningfully per-destination,
 * not shared graph state the way every section below is.
 */
@HiltViewModel
class ExamViewModel @Inject constructor(
    getExamSchedules: GetExamSchedulesUseCase,
    getExamResults: GetExamResultsUseCase,
    getRevaluationRequests: GetRevaluationRequestsUseCase,
    getMyPhotocopyRequests: GetMyPhotocopyRequestsUseCase,
    getMyAppeals: GetMyAppealsUseCase,
    getChallengeRevaluations: GetChallengeRevaluationsUseCase,
    getSupplementaryExams: GetSupplementaryExamsUseCase,
    getReappearExams: GetReappearExamsUseCase,
    getMyMalpracticeCases: GetMyMalpracticeCasesUseCase,
    private val submitRevaluationRequestUseCase: SubmitRevaluationRequestUseCase,
    private val submitPhotocopyRequestUseCase: SubmitPhotocopyRequestUseCase,
    private val submitAppealUseCase: SubmitAppealUseCase,
    private val submitChallengeRevaluationUseCase: SubmitChallengeRevaluationUseCase,
    private val registerForSupplementaryExamUseCase: RegisterForSupplementaryExamUseCase,
    private val registerForReappearExamUseCase: RegisterForReappearExamUseCase
) : ViewModel() {

    val schedules: SectionLoader<List<ExamSchedule>> =
        SectionLoader(viewModelScope, "exam schedules") { getExamSchedules() }
    val results: SectionLoader<List<ExamResult>> =
        SectionLoader(viewModelScope, "exam results") { getExamResults() }
    val revaluationRequests: SectionLoader<List<RevaluationRequest>> =
        SectionLoader(viewModelScope, "revaluation requests") { getRevaluationRequests() }
    val photocopyRequests: SectionLoader<List<PhotocopyRequest>> =
        SectionLoader(viewModelScope, "photocopy requests") { getMyPhotocopyRequests() }
    val appeals: SectionLoader<List<ExamAppeal>> =
        SectionLoader(viewModelScope, "revaluation appeals") { getMyAppeals() }
    val challengeRevaluations: SectionLoader<List<ChallengeRevaluation>> =
        SectionLoader(viewModelScope, "challenge revaluations") { getChallengeRevaluations() }
    val supplementaryExams: SectionLoader<List<SupplementaryExam>> =
        SectionLoader(viewModelScope, "supplementary exams") { getSupplementaryExams() }
    val reappearExams: SectionLoader<List<ReappearExam>> =
        SectionLoader(viewModelScope, "reappear exams") { getReappearExams() }
    val malpracticeCases: SectionLoader<List<MalpracticeCase>> =
        SectionLoader(viewModelScope, "malpractice cases") { getMyMalpracticeCases() }

    private val _effect = MutableSharedFlow<ExamEffect>()
    val effect: SharedFlow<ExamEffect> = _effect.asSharedFlow()

    fun loadSchedules() = schedules.loadOnce()
    fun loadResults() = results.loadOnce()

    fun loadRevaluation() {
        revaluationRequests.loadOnce()
        photocopyRequests.loadOnce()
        appeals.loadOnce()
        challengeRevaluations.loadOnce()
    }

    fun loadReExams() {
        supplementaryExams.loadOnce()
        reappearExams.loadOnce()
    }

    fun loadMalpractice() = malpracticeCases.loadOnce()

    fun submitRevaluationRequest(scheduleId: String, courseId: String, reason: String?) {
        submit("Revaluation request submitted") {
            submitRevaluationRequestUseCase(scheduleId, courseId, reason)
            revaluationRequests.reload()
        }
    }

    fun submitPhotocopyRequest(scheduleId: String, courseId: String) {
        submit("Photocopy request submitted") {
            submitPhotocopyRequestUseCase(scheduleId, courseId)
            photocopyRequests.reload()
        }
    }

    fun submitAppeal(referenceId: String, reason: String) {
        submit("Appeal filed") {
            submitAppealUseCase(referenceId, reason)
            appeals.reload()
        }
    }

    fun submitChallengeRevaluation(scheduleId: String, courseId: String, revaluationRequestId: String) {
        submit("Challenge revaluation submitted") {
            submitChallengeRevaluationUseCase(scheduleId, courseId, revaluationRequestId)
            challengeRevaluations.reload()
        }
    }

    fun registerForSupplementaryExam(supplementaryExamId: String, courseIds: List<String>) {
        submit("Registered for supplementary exam") {
            registerForSupplementaryExamUseCase(supplementaryExamId, courseIds)
            supplementaryExams.reload()
        }
    }

    fun registerForReappearExam(reappearExamId: String, courseIds: List<String>) {
        submit("Registered for reappear exam") {
            registerForReappearExamUseCase(reappearExamId, courseIds)
            reappearExams.reload()
        }
    }

    /** Every write action here is a one-shot toast, not a screen-replacing error state -
     * unlike a section's own [SectionLoader.state], a failed submit shouldn't blank out
     * data the student can already see, it should just tell them the submit didn't
     * take. [APIError]'s three variants all carry a usable [Throwable.message]. */
    private fun submit(successMessage: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
                _effect.emit(ExamEffect.ShowToast(successMessage))
            } catch (e: APIError) {
                _effect.emit(ExamEffect.ShowToast(e.message ?: "Something went wrong"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _effect.emit(ExamEffect.ShowToast("Something went wrong"))
            }
        }
    }
}
