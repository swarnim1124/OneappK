package com.xsc.oneapp.feature.exam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.core.result.SectionLoader
import com.xsc.oneapp.feature.exam.data.network.ExamEndpoint
import com.xsc.oneapp.feature.exam.domain.model.EvaluationBundle
import com.xsc.oneapp.feature.exam.domain.model.ExamCentre
import com.xsc.oneapp.feature.exam.domain.model.ExternalEvaluation
import com.xsc.oneapp.feature.exam.domain.model.ExternalExaminer
import com.xsc.oneapp.feature.exam.domain.model.ExternalPaperSetting
import com.xsc.oneapp.feature.exam.domain.model.Grade
import com.xsc.oneapp.feature.exam.domain.model.InternalExam
import com.xsc.oneapp.feature.exam.domain.model.InvigilatorAssignment
import com.xsc.oneapp.feature.exam.domain.model.MarksRecord
import com.xsc.oneapp.feature.exam.domain.model.QuestionBankEntry
import com.xsc.oneapp.feature.exam.domain.model.QuestionPaper
import com.xsc.oneapp.feature.exam.domain.model.QuestionPaperSubmission
import com.xsc.oneapp.feature.exam.domain.model.SeatingPlan
import com.xsc.oneapp.feature.exam.domain.model.StudentExamBlock
import com.xsc.oneapp.feature.exam.domain.usecase.AddEvaluationBundleUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.AddExamCentreUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.AddExamScheduleUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.AddExternalEvaluationUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.AddExternalExaminerUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.AddExternalPaperSettingUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.AddInternalExamUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.AddInvigilatorAssignmentUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.AddQuestionBankEntryUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.AddQuestionPaperUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.AddSeatingPlanUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.AddStudentExamBlockUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.ApplyModerationUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.ApproveQuestionPaperUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.ApproveResultsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.ConsolidateInternalMarksUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.CreateReappearExamUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.CreateSupplementaryExamUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.DeleteEvaluationBundleUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.DeleteExamCentreUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.DeleteExamScheduleUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.DeleteExternalEvaluationUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.DeleteExternalExaminerUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.DeleteExternalPaperSettingUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.DeleteHallTicketAdminUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.DeleteInternalExamUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.DeleteInvigilatorAssignmentUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.DeleteQuestionBankEntryUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.DeleteQuestionPaperSubmissionUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.DeleteQuestionPaperUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.DeleteSeatingPlanUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.DeleteStudentExamBlockUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.DismissMalpracticeCaseUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GenerateGradesUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GenerateHallTicketUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GenerateResultsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetEvaluationBundlesUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetExamBlocksAdminUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetExamCentresUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetExternalEvaluationsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetExternalExaminersUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetExternalPaperSettingsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetGradesUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetInternalExamsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetInternalMarksEntriesUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetInvigilatorAssignmentsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetMarksEntriesUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetQuestionBankUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetQuestionPaperSubmissionsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetQuestionPapersUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetSeatingPlansUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.HoldExamScheduleUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.HoldHallTicketsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.LockGradesUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.ProcessRevaluationUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.PublishExamScheduleUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.PublishHallTicketsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.HoldResultPublicationUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.PublishResultsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.UnpublishResultsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.RecordMalpracticeVerdictUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.RejectQuestionPaperUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.ReportMalpracticeCaseUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.RequestSecondValuationUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.SetReappearEligibilityUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.SubmitInternalMarksEntryUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.SubmitMarksEntryUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.SubmitQuestionPaperUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.UnpublishExamScheduleUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.UnpublishHallTicketsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.UpdateEvaluationBundleUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.UpdateExamCentreUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.UpdateExamScheduleUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.UpdateExternalEvaluationUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.UpdateExternalExaminerUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.UpdateHallTicketAdminUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.UpdateInternalExamUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.UpdateInvigilatorAssignmentUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.UpdateQuestionBankEntryUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.UpdateQuestionPaperUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.UpdateSeatingPlanUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.UpdateStudentExamBlockUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.VerifyMarksUseCase
import com.xsc.oneapp.feature.exam.ui.state.ExamAdminPermissions
import com.xsc.oneapp.feature.exam.ui.state.ExamEffect
import com.xsc.sdk.auth.SessionManager
import com.xsc.sdk.auth.requiredPermissionFor
import com.xsc.sdk.network.APIError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * One ViewModel for the whole examination-administration surface, graph-scoped like
 * [ExamViewModel] but kept separate from it: ~50 gated admin actions belong to a
 * genuinely different screen family than the six student screens, and folding both
 * into one ViewModel would make it unreadable. Every read section is an independent
 * [SectionLoader], same discipline as [ExamViewModel] and `FeeViewModel`.
 */
@HiltViewModel
class ExamAdminViewModel @Inject constructor(
    getExamCentres: GetExamCentresUseCase,
    getSeatingPlans: GetSeatingPlansUseCase,
    getInvigilatorAssignments: GetInvigilatorAssignmentsUseCase,
    getExamBlocksAdmin: GetExamBlocksAdminUseCase,
    getQuestionPapers: GetQuestionPapersUseCase,
    getQuestionPaperSubmissions: GetQuestionPaperSubmissionsUseCase,
    getQuestionBank: GetQuestionBankUseCase,
    getExternalExaminers: GetExternalExaminersUseCase,
    getExternalPaperSettings: GetExternalPaperSettingsUseCase,
    getExternalEvaluations: GetExternalEvaluationsUseCase,
    getInternalExams: GetInternalExamsUseCase,
    getInternalMarksEntries: GetInternalMarksEntriesUseCase,
    getMarksEntries: GetMarksEntriesUseCase,
    getGrades: GetGradesUseCase,
    getEvaluationBundles: GetEvaluationBundlesUseCase,
    private val addExamCentreUseCase: AddExamCentreUseCase,
    private val updateExamCentreUseCase: UpdateExamCentreUseCase,
    private val deleteExamCentreUseCase: DeleteExamCentreUseCase,
    private val addExamScheduleUseCase: AddExamScheduleUseCase,
    private val updateExamScheduleUseCase: UpdateExamScheduleUseCase,
    private val deleteExamScheduleUseCase: DeleteExamScheduleUseCase,
    private val publishExamScheduleUseCase: PublishExamScheduleUseCase,
    private val holdExamScheduleUseCase: HoldExamScheduleUseCase,
    private val unpublishExamScheduleUseCase: UnpublishExamScheduleUseCase,
    private val addSeatingPlanUseCase: AddSeatingPlanUseCase,
    private val updateSeatingPlanUseCase: UpdateSeatingPlanUseCase,
    private val deleteSeatingPlanUseCase: DeleteSeatingPlanUseCase,
    private val addInvigilatorAssignmentUseCase: AddInvigilatorAssignmentUseCase,
    private val updateInvigilatorAssignmentUseCase: UpdateInvigilatorAssignmentUseCase,
    private val deleteInvigilatorAssignmentUseCase: DeleteInvigilatorAssignmentUseCase,
    private val generateHallTicketUseCase: GenerateHallTicketUseCase,
    private val updateHallTicketAdminUseCase: UpdateHallTicketAdminUseCase,
    private val deleteHallTicketAdminUseCase: DeleteHallTicketAdminUseCase,
    private val publishHallTicketsUseCase: PublishHallTicketsUseCase,
    private val holdHallTicketsUseCase: HoldHallTicketsUseCase,
    private val unpublishHallTicketsUseCase: UnpublishHallTicketsUseCase,
    private val addStudentExamBlockUseCase: AddStudentExamBlockUseCase,
    private val updateStudentExamBlockUseCase: UpdateStudentExamBlockUseCase,
    private val deleteStudentExamBlockUseCase: DeleteStudentExamBlockUseCase,
    private val addQuestionPaperUseCase: AddQuestionPaperUseCase,
    private val updateQuestionPaperUseCase: UpdateQuestionPaperUseCase,
    private val deleteQuestionPaperUseCase: DeleteQuestionPaperUseCase,
    private val submitQuestionPaperUseCase: SubmitQuestionPaperUseCase,
    private val deleteQuestionPaperSubmissionUseCase: DeleteQuestionPaperSubmissionUseCase,
    private val approveQuestionPaperUseCase: ApproveQuestionPaperUseCase,
    private val rejectQuestionPaperUseCase: RejectQuestionPaperUseCase,
    private val addQuestionBankEntryUseCase: AddQuestionBankEntryUseCase,
    private val updateQuestionBankEntryUseCase: UpdateQuestionBankEntryUseCase,
    private val deleteQuestionBankEntryUseCase: DeleteQuestionBankEntryUseCase,
    private val addExternalExaminerUseCase: AddExternalExaminerUseCase,
    private val updateExternalExaminerUseCase: UpdateExternalExaminerUseCase,
    private val deleteExternalExaminerUseCase: DeleteExternalExaminerUseCase,
    private val addExternalPaperSettingUseCase: AddExternalPaperSettingUseCase,
    private val deleteExternalPaperSettingUseCase: DeleteExternalPaperSettingUseCase,
    private val addExternalEvaluationUseCase: AddExternalEvaluationUseCase,
    private val updateExternalEvaluationUseCase: UpdateExternalEvaluationUseCase,
    private val deleteExternalEvaluationUseCase: DeleteExternalEvaluationUseCase,
    private val addInternalExamUseCase: AddInternalExamUseCase,
    private val updateInternalExamUseCase: UpdateInternalExamUseCase,
    private val deleteInternalExamUseCase: DeleteInternalExamUseCase,
    private val submitInternalMarksEntryUseCase: SubmitInternalMarksEntryUseCase,
    private val consolidateInternalMarksUseCase: ConsolidateInternalMarksUseCase,
    private val submitMarksEntryUseCase: SubmitMarksEntryUseCase,
    private val verifyMarksUseCase: VerifyMarksUseCase,
    private val generateGradesUseCase: GenerateGradesUseCase,
    private val lockGradesUseCase: LockGradesUseCase,
    private val addEvaluationBundleUseCase: AddEvaluationBundleUseCase,
    private val updateEvaluationBundleUseCase: UpdateEvaluationBundleUseCase,
    private val deleteEvaluationBundleUseCase: DeleteEvaluationBundleUseCase,
    private val requestSecondValuationUseCase: RequestSecondValuationUseCase,
    private val applyModerationUseCase: ApplyModerationUseCase,
    private val generateResultsUseCase: GenerateResultsUseCase,
    private val approveResultsUseCase: ApproveResultsUseCase,
    private val publishResultsUseCase: PublishResultsUseCase,
    private val holdResultPublicationUseCase: HoldResultPublicationUseCase,
    private val unpublishResultsUseCase: UnpublishResultsUseCase,
    private val processRevaluationUseCase: ProcessRevaluationUseCase,
    private val createSupplementaryExamUseCase: CreateSupplementaryExamUseCase,
    private val createReappearExamUseCase: CreateReappearExamUseCase,
    private val setReappearEligibilityUseCase: SetReappearEligibilityUseCase,
    private val reportMalpracticeCaseUseCase: ReportMalpracticeCaseUseCase,
    private val recordMalpracticeVerdictUseCase: RecordMalpracticeVerdictUseCase,
    private val dismissMalpracticeCaseUseCase: DismissMalpracticeCaseUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    val examCentres: SectionLoader<List<ExamCentre>> =
        SectionLoader(viewModelScope, "exam centres") { getExamCentres() }
    val seatingPlans: SectionLoader<List<SeatingPlan>> =
        SectionLoader(viewModelScope, "seating plans") { getSeatingPlans() }
    val invigilatorAssignments: SectionLoader<List<InvigilatorAssignment>> =
        SectionLoader(viewModelScope, "invigilator assignments") { getInvigilatorAssignments() }
    val examBlocksAdmin: SectionLoader<List<StudentExamBlock>> =
        SectionLoader(viewModelScope, "exam blocks") { getExamBlocksAdmin() }
    val questionPapers: SectionLoader<List<QuestionPaper>> =
        SectionLoader(viewModelScope, "question papers") { getQuestionPapers() }
    val questionPaperSubmissions: SectionLoader<List<QuestionPaperSubmission>> =
        SectionLoader(viewModelScope, "question paper submissions") { getQuestionPaperSubmissions() }
    val questionBank: SectionLoader<List<QuestionBankEntry>> =
        SectionLoader(viewModelScope, "question bank") { getQuestionBank() }
    val externalExaminers: SectionLoader<List<ExternalExaminer>> =
        SectionLoader(viewModelScope, "external examiners") { getExternalExaminers() }
    val externalPaperSettings: SectionLoader<List<ExternalPaperSetting>> =
        SectionLoader(viewModelScope, "external paper settings") { getExternalPaperSettings() }
    val externalEvaluations: SectionLoader<List<ExternalEvaluation>> =
        SectionLoader(viewModelScope, "external evaluations") { getExternalEvaluations() }
    val internalExams: SectionLoader<List<InternalExam>> =
        SectionLoader(viewModelScope, "internal exams") { getInternalExams() }
    val internalMarksEntries: SectionLoader<List<MarksRecord>> =
        SectionLoader(viewModelScope, "internal marks entries") { getInternalMarksEntries() }
    val marksEntries: SectionLoader<List<MarksRecord>> =
        SectionLoader(viewModelScope, "marks entries") { getMarksEntries() }
    val grades: SectionLoader<List<Grade>> =
        SectionLoader(viewModelScope, "grades") { getGrades() }
    val evaluationBundles: SectionLoader<List<EvaluationBundle>> =
        SectionLoader(viewModelScope, "evaluation bundles") { getEvaluationBundles() }

    /** Same contract as `FeePermissions`: gates *visibility* only, never send-safety -
     * the backend remains the only real gate. Re-derived on every permissions change. */
    val permissions: StateFlow<ExamAdminPermissions> = sessionManager.currentPermissions
        .map {
            ExamAdminPermissions(
                canAddExamCentre = granted(ExamEndpoint.SubModules.EXAM_CENTER, ExamEndpoint.Actions.EXAM_CENTRE, ExamEndpoint.ActionTypes.ADD),
                canUpdateExamCentre = granted(ExamEndpoint.SubModules.EXAM_CENTER, ExamEndpoint.Actions.EXAM_CENTRE, ExamEndpoint.ActionTypes.UPDATE),
                canDeleteExamCentre = granted(ExamEndpoint.SubModules.EXAM_CENTER, ExamEndpoint.Actions.EXAM_CENTRE, ExamEndpoint.ActionTypes.DELETE),
                canAddExamSchedule = granted(ExamEndpoint.SubModules.SCHEDULE, ExamEndpoint.Actions.EXAM_SCHEDULE, ExamEndpoint.ActionTypes.ADD),
                canUpdateExamSchedule = granted(ExamEndpoint.SubModules.SCHEDULE, ExamEndpoint.Actions.EXAM_SCHEDULE, ExamEndpoint.ActionTypes.UPDATE),
                canDeleteExamSchedule = granted(ExamEndpoint.SubModules.SCHEDULE, ExamEndpoint.Actions.EXAM_SCHEDULE, ExamEndpoint.ActionTypes.DELETE),
                canPublishExamSchedule = granted(ExamEndpoint.SubModules.SCHEDULE, ExamEndpoint.Actions.EXAM_SCHEDULE_PUBLICATION, ExamEndpoint.ActionTypes.PUBLISH),
                canAddSeatingPlan = granted(ExamEndpoint.SubModules.SEATING, ExamEndpoint.Actions.SEATING_PLAN, ExamEndpoint.ActionTypes.ADD),
                canUpdateSeatingPlan = granted(ExamEndpoint.SubModules.SEATING, ExamEndpoint.Actions.SEATING_PLAN, ExamEndpoint.ActionTypes.UPDATE),
                canDeleteSeatingPlan = granted(ExamEndpoint.SubModules.SEATING, ExamEndpoint.Actions.SEATING_PLAN, ExamEndpoint.ActionTypes.DELETE),
                canAddInvigilatorAssignment = granted(ExamEndpoint.SubModules.INVIGILATION, ExamEndpoint.Actions.INVIGILATOR_ASSIGNMENT, ExamEndpoint.ActionTypes.ADD),
                canUpdateInvigilatorAssignment = granted(ExamEndpoint.SubModules.INVIGILATION, ExamEndpoint.Actions.INVIGILATOR_ASSIGNMENT, ExamEndpoint.ActionTypes.UPDATE),
                canDeleteInvigilatorAssignment = granted(ExamEndpoint.SubModules.INVIGILATION, ExamEndpoint.Actions.INVIGILATOR_ASSIGNMENT, ExamEndpoint.ActionTypes.DELETE),
                canGenerateHallTicket = granted(ExamEndpoint.SubModules.HALL_TICKET, ExamEndpoint.Actions.HALL_TICKET, ExamEndpoint.ActionTypes.ADD),
                canUpdateHallTicketAdmin = granted(ExamEndpoint.SubModules.HALL_TICKET, ExamEndpoint.Actions.HALL_TICKET, ExamEndpoint.ActionTypes.UPDATE),
                canDeleteHallTicketAdmin = granted(ExamEndpoint.SubModules.HALL_TICKET, ExamEndpoint.Actions.HALL_TICKET, ExamEndpoint.ActionTypes.DELETE),
                canPublishHallTickets = granted(ExamEndpoint.SubModules.HALL_TICKET, ExamEndpoint.Actions.HALL_TICKET_PUBLICATION, ExamEndpoint.ActionTypes.PUBLISH),
                canAddStudentExamBlock = granted(ExamEndpoint.SubModules.HALL_TICKET, ExamEndpoint.Actions.STUDENT_BLOCK, ExamEndpoint.ActionTypes.ADD),
                canUpdateStudentExamBlock = granted(ExamEndpoint.SubModules.HALL_TICKET, ExamEndpoint.Actions.STUDENT_BLOCK, ExamEndpoint.ActionTypes.UPDATE),
                canDeleteStudentExamBlock = granted(ExamEndpoint.SubModules.HALL_TICKET, ExamEndpoint.Actions.STUDENT_BLOCK, ExamEndpoint.ActionTypes.DELETE),
                canAddQuestionPaper = granted(ExamEndpoint.SubModules.QUESTION_PAPER, ExamEndpoint.Actions.QUESTION_PAPER, ExamEndpoint.ActionTypes.ADD),
                canUpdateQuestionPaper = granted(ExamEndpoint.SubModules.QUESTION_PAPER, ExamEndpoint.Actions.QUESTION_PAPER, ExamEndpoint.ActionTypes.UPDATE),
                canDeleteQuestionPaper = granted(ExamEndpoint.SubModules.QUESTION_PAPER, ExamEndpoint.Actions.QUESTION_PAPER, ExamEndpoint.ActionTypes.DELETE),
                canSubmitQuestionPaper = granted(ExamEndpoint.SubModules.QUESTION_PAPER, ExamEndpoint.Actions.QUESTION_PAPER_SUBMISSION, ExamEndpoint.ActionTypes.ADD),
                canDeleteQuestionPaperSubmission = granted(ExamEndpoint.SubModules.QUESTION_PAPER, ExamEndpoint.Actions.QUESTION_PAPER_SUBMISSION, ExamEndpoint.ActionTypes.DELETE),
                canApproveQuestionPaper = granted(ExamEndpoint.SubModules.QUESTION_PAPER, ExamEndpoint.Actions.QUESTION_PAPER_APPROVAL, ExamEndpoint.ActionTypes.ADD),
                canAddQuestionBankEntry = granted(ExamEndpoint.SubModules.QUESTION_PAPER, ExamEndpoint.Actions.QUESTION_BANK, ExamEndpoint.ActionTypes.ADD),
                canUpdateQuestionBankEntry = granted(ExamEndpoint.SubModules.QUESTION_PAPER, ExamEndpoint.Actions.QUESTION_BANK, ExamEndpoint.ActionTypes.UPDATE),
                canDeleteQuestionBankEntry = granted(ExamEndpoint.SubModules.QUESTION_PAPER, ExamEndpoint.Actions.QUESTION_BANK, ExamEndpoint.ActionTypes.DELETE),
                canAddExternalExaminer = granted(ExamEndpoint.SubModules.EXTERNAL_EXAM, ExamEndpoint.Actions.EXTERNAL_EXAMINER, ExamEndpoint.ActionTypes.ADD),
                canUpdateExternalExaminer = granted(ExamEndpoint.SubModules.EXTERNAL_EXAM, ExamEndpoint.Actions.EXTERNAL_EXAMINER, ExamEndpoint.ActionTypes.UPDATE),
                canDeleteExternalExaminer = granted(ExamEndpoint.SubModules.EXTERNAL_EXAM, ExamEndpoint.Actions.EXTERNAL_EXAMINER, ExamEndpoint.ActionTypes.DELETE),
                canAddExternalPaperSetting = granted(ExamEndpoint.SubModules.EXTERNAL_EXAM, ExamEndpoint.Actions.EXTERNAL_PAPER_SETTING, ExamEndpoint.ActionTypes.ADD),
                canDeleteExternalPaperSetting = granted(ExamEndpoint.SubModules.EXTERNAL_EXAM, ExamEndpoint.Actions.EXTERNAL_PAPER_SETTING, ExamEndpoint.ActionTypes.DELETE),
                canAddExternalEvaluation = granted(ExamEndpoint.SubModules.EXTERNAL_EXAM, ExamEndpoint.Actions.EXTERNAL_EVALUATION, ExamEndpoint.ActionTypes.ADD),
                canUpdateExternalEvaluation = granted(ExamEndpoint.SubModules.EXTERNAL_EXAM, ExamEndpoint.Actions.EXTERNAL_EVALUATION, ExamEndpoint.ActionTypes.UPDATE),
                canDeleteExternalEvaluation = granted(ExamEndpoint.SubModules.EXTERNAL_EXAM, ExamEndpoint.Actions.EXTERNAL_EVALUATION, ExamEndpoint.ActionTypes.DELETE),
                canAddInternalExam = granted(ExamEndpoint.SubModules.INTERNAL_EXAM, ExamEndpoint.Actions.INTERNAL_EXAM, ExamEndpoint.ActionTypes.ADD),
                canUpdateInternalExam = granted(ExamEndpoint.SubModules.INTERNAL_EXAM, ExamEndpoint.Actions.INTERNAL_EXAM, ExamEndpoint.ActionTypes.UPDATE),
                canDeleteInternalExam = granted(ExamEndpoint.SubModules.INTERNAL_EXAM, ExamEndpoint.Actions.INTERNAL_EXAM, ExamEndpoint.ActionTypes.DELETE),
                canSubmitInternalMarksEntry = granted(ExamEndpoint.SubModules.INTERNAL_EXAM, ExamEndpoint.Actions.INTERNAL_MARKS_ENTRY, ExamEndpoint.ActionTypes.ADD),
                canConsolidateInternalMarks = granted(ExamEndpoint.SubModules.INTERNAL_EXAM, ExamEndpoint.Actions.INTERNAL_MARKS_CONSOLIDATION, ExamEndpoint.ActionTypes.ADD),
                canSubmitMarksEntry = granted(ExamEndpoint.SubModules.MARKS, ExamEndpoint.Actions.MARKS_ENTRY, ExamEndpoint.ActionTypes.ADD),
                canVerifyMarks = granted(ExamEndpoint.SubModules.MARKS, ExamEndpoint.Actions.MARKS_VERIFICATION, ExamEndpoint.ActionTypes.ADD),
                canGenerateGrades = granted(ExamEndpoint.SubModules.MARKS, ExamEndpoint.Actions.GRADE, ExamEndpoint.ActionTypes.ADD),
                canLockGrades = granted(ExamEndpoint.SubModules.MARKS, ExamEndpoint.Actions.GRADE_LOCK, ExamEndpoint.ActionTypes.ADD),
                canAddEvaluationBundle = granted(ExamEndpoint.SubModules.EVALUATION, ExamEndpoint.Actions.EVALUATION_BUNDLE, ExamEndpoint.ActionTypes.ADD),
                canUpdateEvaluationBundle = granted(ExamEndpoint.SubModules.EVALUATION, ExamEndpoint.Actions.EVALUATION_BUNDLE, ExamEndpoint.ActionTypes.UPDATE),
                canDeleteEvaluationBundle = granted(ExamEndpoint.SubModules.EVALUATION, ExamEndpoint.Actions.EVALUATION_BUNDLE, ExamEndpoint.ActionTypes.DELETE),
                canRequestSecondValuation = granted(ExamEndpoint.SubModules.EVALUATION, ExamEndpoint.Actions.SECOND_VALUATION, ExamEndpoint.ActionTypes.ADD),
                canApplyModeration = granted(ExamEndpoint.SubModules.EVALUATION, ExamEndpoint.Actions.MODERATION, ExamEndpoint.ActionTypes.ADD),
                canGenerateResults = granted(ExamEndpoint.SubModules.RESULTS, ExamEndpoint.Actions.RESULT, ExamEndpoint.ActionTypes.ADD),
                canApproveResults = granted(ExamEndpoint.SubModules.RESULT_APPROVAL, ExamEndpoint.Actions.RESULT_APPROVAL, ExamEndpoint.ActionTypes.ADD),
                canPublishResults = granted(ExamEndpoint.SubModules.RESULT_APPROVAL, ExamEndpoint.Actions.RESULT_PUBLICATION, ExamEndpoint.ActionTypes.PUBLISH),
                canProcessRevaluation = granted(ExamEndpoint.SubModules.REVALUATION, ExamEndpoint.Actions.REVALUATION_PROCESSING, ExamEndpoint.ActionTypes.ADD),
                canCreateSupplementaryExam = granted(ExamEndpoint.SubModules.SUPPLEMENTARY, ExamEndpoint.Actions.SUPPLEMENTARY_EXAM, ExamEndpoint.ActionTypes.ADD),
                canCreateReappearExam = granted(ExamEndpoint.SubModules.REAPPEAR, ExamEndpoint.Actions.REAPPEAR_EXAM, ExamEndpoint.ActionTypes.ADD),
                canSetReappearEligibility = granted(ExamEndpoint.SubModules.REAPPEAR, ExamEndpoint.Actions.REAPPEAR_ELIGIBILITY, ExamEndpoint.ActionTypes.ADD),
                canReportMalpracticeCase = granted(ExamEndpoint.SubModules.MALPRACTICE, ExamEndpoint.Actions.MALPRACTICE_CASE, ExamEndpoint.ActionTypes.ADD),
                canRecordMalpracticeVerdict = granted(ExamEndpoint.SubModules.MALPRACTICE, ExamEndpoint.Actions.MALPRACTICE_CASE, ExamEndpoint.ActionTypes.UPDATE),
                canDismissMalpracticeCase = granted(ExamEndpoint.SubModules.MALPRACTICE, ExamEndpoint.Actions.MALPRACTICE_CASE, ExamEndpoint.ActionTypes.DELETE)
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ExamAdminPermissions())

    /** Coarse gate for whether the "Exam Administration" entry point renders at all on
     * [ExamScreen] - true the moment any one admin action is granted. */
    val hasAnyExamAdminAccess: StateFlow<Boolean> = sessionManager.currentPermissions
        .map { sessionManager.hasAnyPermission(*ALL_ADMIN_PERMISSIONS) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private fun granted(submodule: String, action: String, actionType: String) =
        sessionManager.hasPermission(requiredPermissionFor(ExamEndpoint.MODULE, submodule, action, actionType))

    private val _effect = MutableSharedFlow<ExamEffect>()
    val effect: SharedFlow<ExamEffect> = _effect.asSharedFlow()

    fun loadExamCentres() = examCentres.loadOnce()
    fun loadSeatingAndInvigilation() {
        seatingPlans.loadOnce()
        invigilatorAssignments.loadOnce()
    }
    fun loadHallTicketAdmin() = examBlocksAdmin.loadOnce()
    fun loadQuestionPaperAdmin() {
        questionPapers.loadOnce()
        questionPaperSubmissions.loadOnce()
        questionBank.loadOnce()
    }
    fun loadExternalExamAdmin() {
        externalExaminers.loadOnce()
        externalPaperSettings.loadOnce()
        externalEvaluations.loadOnce()
    }
    fun loadInternalExamAdmin() {
        internalExams.loadOnce()
        internalMarksEntries.loadOnce()
    }
    fun loadMarksGradingAdmin() {
        marksEntries.loadOnce()
        grades.loadOnce()
    }
    fun loadEvaluationAdmin() = evaluationBundles.loadOnce()

    // ---- sm_examCenter ----
    fun addExamCentre(centreName: String, capacity: String, address: String?, courses: List<String>) =
        submit("Exam centre added") {
            addExamCentreUseCase(centreName, capacity, address, courses)
            examCentres.reload()
        }

    fun updateExamCentre(
        centreId: String,
        centreName: String? = null,
        capacity: String? = null,
        address: String? = null,
        courses: List<String>? = null,
        status: String? = null
    ) = submit("Exam centre updated") {
        updateExamCentreUseCase(centreId, centreName, capacity, address, courses, status)
        examCentres.reload()
    }

    fun deleteExamCentre(centreId: String) = submit("Exam centre removed") {
        deleteExamCentreUseCase(centreId)
        examCentres.reload()
    }

    // ---- sm_schedule ----
    fun addExamSchedule(
        name: String,
        examType: String,
        fromDate: String,
        toDate: String,
        sessionDate: String,
        session: String
    ) = submit("Exam schedule created") {
        addExamScheduleUseCase(name, examType, fromDate, toDate, sessionDate, session)
    }

    fun updateExamSchedule(scheduleId: String, name: String? = null, fromDate: String? = null, toDate: String? = null) =
        submit("Exam schedule updated") { updateExamScheduleUseCase(scheduleId, name, fromDate, toDate) }

    fun deleteExamSchedule(scheduleId: String) =
        submit("Exam schedule removed") { deleteExamScheduleUseCase(scheduleId) }

    fun publishExamSchedule(scheduleId: String) =
        submit("Exam schedule published") { publishExamScheduleUseCase(scheduleId) }

    fun holdExamSchedule(scheduleId: String) =
        submit("Exam schedule put on hold") { holdExamScheduleUseCase(scheduleId) }

    fun unpublishExamSchedule(scheduleId: String) =
        submit("Exam schedule unpublished") { unpublishExamScheduleUseCase(scheduleId) }

    // ---- sm_seating ----
    fun addSeatingPlan(
        scheduleId: String,
        venueId: String,
        courseId: String,
        studentId: String,
        seatNumber: String,
        allocationMethod: String? = null
    ) = submit("Seat allocated") {
        addSeatingPlanUseCase(scheduleId, venueId, courseId, studentId, seatNumber, allocationMethod)
        seatingPlans.reload()
    }

    fun updateSeatingPlan(id: String, seatNumber: String? = null, venueId: String? = null) =
        submit("Seating updated") {
            updateSeatingPlanUseCase(id, seatNumber, venueId)
            seatingPlans.reload()
        }

    fun deleteSeatingPlan(id: String) = submit("Seat allocation removed") {
        deleteSeatingPlanUseCase(id)
        seatingPlans.reload()
    }

    // ---- sm_invigilation ----
    fun addInvigilatorAssignment(scheduleId: String, venueId: String, facultyId: String, dutyType: String) =
        submit("Invigilator assigned") {
            addInvigilatorAssignmentUseCase(scheduleId, venueId, facultyId, dutyType)
            invigilatorAssignments.reload()
        }

    fun updateInvigilatorAssignment(id: String, dutyType: String? = null, status: String? = null) =
        submit("Assignment updated") {
            updateInvigilatorAssignmentUseCase(id, dutyType, status)
            invigilatorAssignments.reload()
        }

    fun deleteInvigilatorAssignment(id: String) = submit("Assignment removed") {
        deleteInvigilatorAssignmentUseCase(id)
        invigilatorAssignments.reload()
    }

    // ---- sm_hallTicket (admin) ----
    fun generateHallTicket(scheduleId: String, studentId: String, venueDetails: String? = null, seatNumber: String? = null) =
        submit("Hall ticket generated") { generateHallTicketUseCase(scheduleId, studentId, venueDetails, seatNumber) }

    fun updateHallTicketAdmin(id: String, venueDetails: String? = null, seatNumber: String? = null) =
        submit("Hall ticket updated") { updateHallTicketAdminUseCase(id, venueDetails, seatNumber) }

    fun deleteHallTicketAdmin(id: String) =
        submit("Hall ticket removed") { deleteHallTicketAdminUseCase(id) }

    fun publishHallTickets(scheduleId: String) =
        submit("Hall tickets published") { publishHallTicketsUseCase(scheduleId) }

    fun holdHallTickets(scheduleId: String) =
        submit("Hall tickets put on hold") { holdHallTicketsUseCase(scheduleId) }

    fun unpublishHallTickets(scheduleId: String) =
        submit("Hall tickets unpublished") { unpublishHallTicketsUseCase(scheduleId) }

    fun addStudentExamBlock(studentId: String, scheduleId: String, reason: String) =
        submit("Student blocked from exam") {
            addStudentExamBlockUseCase(studentId, scheduleId, reason)
            examBlocksAdmin.reload()
        }

    fun updateStudentExamBlock(id: String, reason: String? = null, status: String? = null) =
        submit("Block updated") {
            updateStudentExamBlockUseCase(id, reason, status)
            examBlocksAdmin.reload()
        }

    fun deleteStudentExamBlock(id: String) = submit("Block lifted") {
        deleteStudentExamBlockUseCase(id)
        examBlocksAdmin.reload()
    }

    // ---- sm_questionPaper ----
    fun addQuestionPaper(scheduleId: String, courseId: String, content: String) =
        submit("Question paper drafted") {
            addQuestionPaperUseCase(scheduleId, courseId, content)
            questionPapers.reload()
        }

    fun updateQuestionPaper(id: String, content: String? = null, status: String? = null) =
        submit("Question paper updated") {
            updateQuestionPaperUseCase(id, content, status)
            questionPapers.reload()
        }

    fun deleteQuestionPaper(id: String) = submit("Question paper removed") {
        deleteQuestionPaperUseCase(id)
        questionPapers.reload()
    }

    fun submitQuestionPaper(questionPaperId: String) = submit("Question paper submitted for review") {
        submitQuestionPaperUseCase(questionPaperId)
        questionPaperSubmissions.reload()
    }

    fun deleteQuestionPaperSubmission(id: String) = submit("Submission withdrawn") {
        deleteQuestionPaperSubmissionUseCase(id)
        questionPaperSubmissions.reload()
    }

    fun approveQuestionPaper(questionPaperId: String) = submit("Question paper approved") {
        approveQuestionPaperUseCase(questionPaperId)
        questionPapers.reload()
    }

    fun rejectQuestionPaper(questionPaperId: String, reason: String? = null) = submit("Question paper rejected") {
        rejectQuestionPaperUseCase(questionPaperId, reason)
        questionPapers.reload()
    }

    fun addQuestionBankEntry(courseId: String, content: String, questionPaperId: String? = null) =
        submit("Added to question bank") {
            addQuestionBankEntryUseCase(courseId, content, questionPaperId)
            questionBank.reload()
        }

    fun updateQuestionBankEntry(id: String, content: String? = null) = submit("Question bank entry updated") {
        updateQuestionBankEntryUseCase(id, content)
        questionBank.reload()
    }

    fun deleteQuestionBankEntry(id: String) = submit("Removed from question bank") {
        deleteQuestionBankEntryUseCase(id)
        questionBank.reload()
    }

    // ---- sm_externalExam ----
    fun addExternalExaminer(
        examinerName: String,
        contactEmail: String? = null,
        contactPhone: String? = null,
        specialization: String? = null
    ) = submit("Examiner registered") {
        addExternalExaminerUseCase(examinerName, contactEmail, contactPhone, specialization)
        externalExaminers.reload()
    }

    fun updateExternalExaminer(
        id: String,
        examinerName: String? = null,
        contactEmail: String? = null,
        contactPhone: String? = null,
        specialization: String? = null
    ) = submit("Examiner updated") {
        updateExternalExaminerUseCase(id, examinerName, contactEmail, contactPhone, specialization)
        externalExaminers.reload()
    }

    fun deleteExternalExaminer(id: String) = submit("Examiner removed") {
        deleteExternalExaminerUseCase(id)
        externalExaminers.reload()
    }

    fun addExternalPaperSetting(examinerId: String, courseId: String, scheduleId: String) =
        submit("Paper setting assigned") {
            addExternalPaperSettingUseCase(examinerId, courseId, scheduleId)
            externalPaperSettings.reload()
        }

    fun deleteExternalPaperSetting(id: String) = submit("Paper setting assignment removed") {
        deleteExternalPaperSettingUseCase(id)
        externalPaperSettings.reload()
    }

    fun addExternalEvaluation(examinerId: String, courseId: String, scheduleId: String, scriptBundleIds: List<String>) =
        submit("Evaluation dispatched") {
            addExternalEvaluationUseCase(examinerId, courseId, scheduleId, scriptBundleIds)
            externalEvaluations.reload()
        }

    fun updateExternalEvaluation(id: String, status: String? = null) = submit("Evaluation updated") {
        updateExternalEvaluationUseCase(id, status)
        externalEvaluations.reload()
    }

    fun deleteExternalEvaluation(id: String) = submit("Evaluation removed") {
        deleteExternalEvaluationUseCase(id)
        externalEvaluations.reload()
    }

    // ---- sm_internalExam ----
    fun addInternalExam(courseOfferingId: String, examName: String, examTypeId: String, examDate: String, totalMaxMarks: String) =
        submit("Internal exam created") {
            addInternalExamUseCase(courseOfferingId, examName, examTypeId, examDate, totalMaxMarks)
            internalExams.reload()
        }

    fun updateInternalExam(
        id: String,
        examName: String? = null,
        examDate: String? = null,
        totalMaxMarks: String? = null,
        status: String? = null
    ) = submit("Internal exam updated") {
        updateInternalExamUseCase(id, examName, examDate, totalMaxMarks, status)
        internalExams.reload()
    }

    fun deleteInternalExam(id: String) = submit("Internal exam removed") {
        deleteInternalExamUseCase(id)
        internalExams.reload()
    }

    fun submitInternalMarksEntry(internalExamId: String, records: List<Pair<String, String>>) =
        submit("Internal marks submitted") {
            submitInternalMarksEntryUseCase(internalExamId, records)
            internalMarksEntries.reload()
        }

    fun consolidateInternalMarks(courseOfferingId: String) = submit("Internal marks consolidated") {
        consolidateInternalMarksUseCase(courseOfferingId)
        internalMarksEntries.reload()
    }

    // ---- sm_marks ----
    fun submitMarksEntry(scheduleId: String, courseId: String, records: List<Pair<String, String>>) =
        submit("Marks submitted") {
            submitMarksEntryUseCase(scheduleId, courseId, records)
            marksEntries.reload()
        }

    fun verifyMarks(scheduleId: String, courseId: String) = submit("Marks verified") {
        verifyMarksUseCase(scheduleId, courseId)
        marksEntries.reload()
    }

    fun generateGrades(scheduleId: String, courseId: String) = submit("Grades generated") {
        generateGradesUseCase(scheduleId, courseId)
        grades.reload()
    }

    fun lockGrades(scheduleId: String, courseId: String) = submit("Grades locked") {
        lockGradesUseCase(scheduleId, courseId)
        grades.reload()
    }

    // ---- sm_evaluation ----
    fun addEvaluationBundle(scheduleId: String, courseId: String, evaluatorId: String, studentIds: List<String>) =
        submit("Evaluation bundle assigned") {
            addEvaluationBundleUseCase(scheduleId, courseId, evaluatorId, studentIds)
            evaluationBundles.reload()
        }

    fun updateEvaluationBundle(id: String, status: String? = null) = submit("Evaluation bundle updated") {
        updateEvaluationBundleUseCase(id, status)
        evaluationBundles.reload()
    }

    fun deleteEvaluationBundle(id: String) = submit("Evaluation bundle removed") {
        deleteEvaluationBundleUseCase(id)
        evaluationBundles.reload()
    }

    fun requestSecondValuation(scheduleId: String, courseId: String, studentId: String) =
        submit("Second valuation requested") { requestSecondValuationUseCase(scheduleId, courseId, studentId) }

    fun applyModeration(scheduleId: String, courseId: String, adjustment: String) =
        submit("Moderation applied") { applyModerationUseCase(scheduleId, courseId, adjustment) }

    // ---- sm_results / sm_resultApproval ----
    fun generateResults(scheduleId: String) =
        submit("Results generated") { generateResultsUseCase(scheduleId) }

    fun approveResults(scheduleId: String, level: String, remarks: String? = null) =
        submit("Results approved") { approveResultsUseCase(scheduleId, level, remarks) }

    fun publishResults(scheduleId: String) =
        submit("Results published") { publishResultsUseCase(scheduleId) }

    fun holdResultPublication(scheduleId: String) =
        submit("Result publication put on hold") { holdResultPublicationUseCase(scheduleId) }

    fun unpublishResults(scheduleId: String) =
        submit("Results unpublished") { unpublishResultsUseCase(scheduleId) }

    // ---- Folded into student screens ----
    fun processRevaluation(revaluationRequestId: String, evaluatorId: String) =
        submit("Re-evaluator assigned") { processRevaluationUseCase(revaluationRequestId, evaluatorId) }

    fun createSupplementaryExam(name: String, fromDate: String, toDate: String, courseIds: List<String>) =
        submit("Supplementary exam scheduled") { createSupplementaryExamUseCase(name, fromDate, toDate, courseIds) }

    fun createReappearExam(
        name: String,
        academicTermId: String,
        examStartDate: String,
        examEndDate: String,
        registrationStartDate: String,
        registrationEndDate: String,
        courseIds: List<String>
    ) = submit("Reappear exam scheduled") {
        createReappearExamUseCase(
            name, academicTermId, examStartDate, examEndDate,
            registrationStartDate, registrationEndDate, courseIds
        )
    }

    fun setReappearEligibility(
        studentId: String,
        courseId: String,
        reappearExamId: String,
        isEligible: Boolean,
        reason: String? = null
    ) = submit("Eligibility updated") {
        setReappearEligibilityUseCase(studentId, courseId, reappearExamId, isEligible, reason)
    }

    fun reportMalpracticeCase(studentId: String, scheduleId: String, description: String) =
        submit("Malpractice case reported") { reportMalpracticeCaseUseCase(studentId, scheduleId, description) }

    fun recordMalpracticeVerdict(caseId: String, committee: String? = null, hearingDate: String? = null, verdict: String) =
        submit("Verdict recorded") { recordMalpracticeVerdictUseCase(caseId, committee, hearingDate, verdict) }

    fun dismissMalpracticeCase(caseId: String) =
        submit("Case dismissed") { dismissMalpracticeCaseUseCase(caseId) }

    /** Every write action here is a one-shot toast, not a screen-replacing error state -
     * matches [ExamViewModel.submit] and `FeeViewModel.submit` exactly. */
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

    private companion object {
        /** Every admin permission string this ViewModel gates, used only to compute
         * [hasAnyExamAdminAccess] - the fine-grained flags above are the ones each
         * screen actually renders against. */
        val ALL_ADMIN_PERMISSIONS: Array<String> = arrayOf(
            action(ExamEndpoint.SubModules.EXAM_CENTER, ExamEndpoint.Actions.EXAM_CENTRE, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.SCHEDULE, ExamEndpoint.Actions.EXAM_SCHEDULE, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.SCHEDULE, ExamEndpoint.Actions.EXAM_SCHEDULE_PUBLICATION, ExamEndpoint.ActionTypes.PUBLISH),
            action(ExamEndpoint.SubModules.SEATING, ExamEndpoint.Actions.SEATING_PLAN, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.INVIGILATION, ExamEndpoint.Actions.INVIGILATOR_ASSIGNMENT, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.HALL_TICKET, ExamEndpoint.Actions.HALL_TICKET, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.HALL_TICKET, ExamEndpoint.Actions.STUDENT_BLOCK, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.QUESTION_PAPER, ExamEndpoint.Actions.QUESTION_PAPER, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.QUESTION_PAPER, ExamEndpoint.Actions.QUESTION_PAPER_APPROVAL, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.QUESTION_PAPER, ExamEndpoint.Actions.QUESTION_BANK, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.EXTERNAL_EXAM, ExamEndpoint.Actions.EXTERNAL_EXAMINER, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.EXTERNAL_EXAM, ExamEndpoint.Actions.EXTERNAL_PAPER_SETTING, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.EXTERNAL_EXAM, ExamEndpoint.Actions.EXTERNAL_EVALUATION, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.INTERNAL_EXAM, ExamEndpoint.Actions.INTERNAL_EXAM, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.INTERNAL_EXAM, ExamEndpoint.Actions.INTERNAL_MARKS_ENTRY, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.MARKS, ExamEndpoint.Actions.MARKS_ENTRY, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.MARKS, ExamEndpoint.Actions.GRADE, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.MARKS, ExamEndpoint.Actions.GRADE_LOCK, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.EVALUATION, ExamEndpoint.Actions.EVALUATION_BUNDLE, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.EVALUATION, ExamEndpoint.Actions.SECOND_VALUATION, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.EVALUATION, ExamEndpoint.Actions.MODERATION, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.RESULTS, ExamEndpoint.Actions.RESULT, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.RESULT_APPROVAL, ExamEndpoint.Actions.RESULT_APPROVAL, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.RESULT_APPROVAL, ExamEndpoint.Actions.RESULT_PUBLICATION, ExamEndpoint.ActionTypes.PUBLISH),
            action(ExamEndpoint.SubModules.REVALUATION, ExamEndpoint.Actions.REVALUATION_PROCESSING, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.SUPPLEMENTARY, ExamEndpoint.Actions.SUPPLEMENTARY_EXAM, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.REAPPEAR, ExamEndpoint.Actions.REAPPEAR_EXAM, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.REAPPEAR, ExamEndpoint.Actions.REAPPEAR_ELIGIBILITY, ExamEndpoint.ActionTypes.ADD),
            action(ExamEndpoint.SubModules.MALPRACTICE, ExamEndpoint.Actions.MALPRACTICE_CASE, ExamEndpoint.ActionTypes.ADD)
        )

        private fun action(submodule: String, actionName: String, actionType: String) =
            requiredPermissionFor(ExamEndpoint.MODULE, submodule, actionName, actionType)
    }
}
