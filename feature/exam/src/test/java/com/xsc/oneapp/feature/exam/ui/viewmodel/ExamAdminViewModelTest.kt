package com.xsc.oneapp.feature.exam.ui.viewmodel

import app.cash.turbine.test
import com.xsc.oneapp.feature.exam.data.network.ExamEndpoint
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
import com.xsc.oneapp.feature.exam.domain.usecase.HoldResultPublicationUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.LockGradesUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.ProcessRevaluationUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.PublishExamScheduleUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.PublishHallTicketsUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.PublishResultsUseCase
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
import com.xsc.oneapp.feature.exam.domain.usecase.UnpublishResultsUseCase
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
import com.xsc.oneapp.feature.exam.domain.model.ResultApprovalStatus
import com.xsc.oneapp.feature.exam.ui.state.ExamAdminPermissions
import com.xsc.oneapp.feature.exam.ui.state.ExamEffect
import com.xsc.sdk.auth.SessionManager
import com.xsc.sdk.auth.requiredPermissionFor
import com.xsc.sdk.network.APIError
import io.mockk.Runs
import io.mockk.anyVararg
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * ~15 read sections and ~50 gated write actions - too many to exercise one by one, so
 * this covers the shared mechanics FeeViewModelTest already established for this
 * pattern (permission derivation, submit-then-reload, error-to-toast) against a
 * representative slice rather than duplicating the same assertion 50 times.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExamAdminViewModelTest {

    private lateinit var getExamCentresUseCase: GetExamCentresUseCase
    private lateinit var getSeatingPlansUseCase: GetSeatingPlansUseCase
    private lateinit var getInvigilatorAssignmentsUseCase: GetInvigilatorAssignmentsUseCase
    private lateinit var getExamBlocksAdminUseCase: GetExamBlocksAdminUseCase
    private lateinit var getQuestionPapersUseCase: GetQuestionPapersUseCase
    private lateinit var getQuestionPaperSubmissionsUseCase: GetQuestionPaperSubmissionsUseCase
    private lateinit var getQuestionBankUseCase: GetQuestionBankUseCase
    private lateinit var getExternalExaminersUseCase: GetExternalExaminersUseCase
    private lateinit var getExternalPaperSettingsUseCase: GetExternalPaperSettingsUseCase
    private lateinit var getExternalEvaluationsUseCase: GetExternalEvaluationsUseCase
    private lateinit var getInternalExamsUseCase: GetInternalExamsUseCase
    private lateinit var getInternalMarksEntriesUseCase: GetInternalMarksEntriesUseCase
    private lateinit var getMarksEntriesUseCase: GetMarksEntriesUseCase
    private lateinit var getGradesUseCase: GetGradesUseCase
    private lateinit var getEvaluationBundlesUseCase: GetEvaluationBundlesUseCase

    private lateinit var addExamCentreUseCase: AddExamCentreUseCase
    private lateinit var updateExamCentreUseCase: UpdateExamCentreUseCase
    private lateinit var deleteExamCentreUseCase: DeleteExamCentreUseCase
    private lateinit var addExamScheduleUseCase: AddExamScheduleUseCase
    private lateinit var updateExamScheduleUseCase: UpdateExamScheduleUseCase
    private lateinit var deleteExamScheduleUseCase: DeleteExamScheduleUseCase
    private lateinit var publishExamScheduleUseCase: PublishExamScheduleUseCase
    private lateinit var holdExamScheduleUseCase: HoldExamScheduleUseCase
    private lateinit var unpublishExamScheduleUseCase: UnpublishExamScheduleUseCase
    private lateinit var addSeatingPlanUseCase: AddSeatingPlanUseCase
    private lateinit var updateSeatingPlanUseCase: UpdateSeatingPlanUseCase
    private lateinit var deleteSeatingPlanUseCase: DeleteSeatingPlanUseCase
    private lateinit var addInvigilatorAssignmentUseCase: AddInvigilatorAssignmentUseCase
    private lateinit var updateInvigilatorAssignmentUseCase: UpdateInvigilatorAssignmentUseCase
    private lateinit var deleteInvigilatorAssignmentUseCase: DeleteInvigilatorAssignmentUseCase
    private lateinit var generateHallTicketUseCase: GenerateHallTicketUseCase
    private lateinit var updateHallTicketAdminUseCase: UpdateHallTicketAdminUseCase
    private lateinit var deleteHallTicketAdminUseCase: DeleteHallTicketAdminUseCase
    private lateinit var publishHallTicketsUseCase: PublishHallTicketsUseCase
    private lateinit var holdHallTicketsUseCase: HoldHallTicketsUseCase
    private lateinit var unpublishHallTicketsUseCase: UnpublishHallTicketsUseCase
    private lateinit var addStudentExamBlockUseCase: AddStudentExamBlockUseCase
    private lateinit var updateStudentExamBlockUseCase: UpdateStudentExamBlockUseCase
    private lateinit var deleteStudentExamBlockUseCase: DeleteStudentExamBlockUseCase
    private lateinit var addQuestionPaperUseCase: AddQuestionPaperUseCase
    private lateinit var updateQuestionPaperUseCase: UpdateQuestionPaperUseCase
    private lateinit var deleteQuestionPaperUseCase: DeleteQuestionPaperUseCase
    private lateinit var submitQuestionPaperUseCase: SubmitQuestionPaperUseCase
    private lateinit var deleteQuestionPaperSubmissionUseCase: DeleteQuestionPaperSubmissionUseCase
    private lateinit var approveQuestionPaperUseCase: ApproveQuestionPaperUseCase
    private lateinit var rejectQuestionPaperUseCase: RejectQuestionPaperUseCase
    private lateinit var addQuestionBankEntryUseCase: AddQuestionBankEntryUseCase
    private lateinit var updateQuestionBankEntryUseCase: UpdateQuestionBankEntryUseCase
    private lateinit var deleteQuestionBankEntryUseCase: DeleteQuestionBankEntryUseCase
    private lateinit var addExternalExaminerUseCase: AddExternalExaminerUseCase
    private lateinit var updateExternalExaminerUseCase: UpdateExternalExaminerUseCase
    private lateinit var deleteExternalExaminerUseCase: DeleteExternalExaminerUseCase
    private lateinit var addExternalPaperSettingUseCase: AddExternalPaperSettingUseCase
    private lateinit var deleteExternalPaperSettingUseCase: DeleteExternalPaperSettingUseCase
    private lateinit var addExternalEvaluationUseCase: AddExternalEvaluationUseCase
    private lateinit var updateExternalEvaluationUseCase: UpdateExternalEvaluationUseCase
    private lateinit var deleteExternalEvaluationUseCase: DeleteExternalEvaluationUseCase
    private lateinit var addInternalExamUseCase: AddInternalExamUseCase
    private lateinit var updateInternalExamUseCase: UpdateInternalExamUseCase
    private lateinit var deleteInternalExamUseCase: DeleteInternalExamUseCase
    private lateinit var submitInternalMarksEntryUseCase: SubmitInternalMarksEntryUseCase
    private lateinit var consolidateInternalMarksUseCase: ConsolidateInternalMarksUseCase
    private lateinit var submitMarksEntryUseCase: SubmitMarksEntryUseCase
    private lateinit var verifyMarksUseCase: VerifyMarksUseCase
    private lateinit var generateGradesUseCase: GenerateGradesUseCase
    private lateinit var lockGradesUseCase: LockGradesUseCase
    private lateinit var addEvaluationBundleUseCase: AddEvaluationBundleUseCase
    private lateinit var updateEvaluationBundleUseCase: UpdateEvaluationBundleUseCase
    private lateinit var deleteEvaluationBundleUseCase: DeleteEvaluationBundleUseCase
    private lateinit var requestSecondValuationUseCase: RequestSecondValuationUseCase
    private lateinit var applyModerationUseCase: ApplyModerationUseCase
    private lateinit var generateResultsUseCase: GenerateResultsUseCase
    private lateinit var approveResultsUseCase: ApproveResultsUseCase
    private lateinit var publishResultsUseCase: PublishResultsUseCase
    private lateinit var holdResultPublicationUseCase: HoldResultPublicationUseCase
    private lateinit var unpublishResultsUseCase: UnpublishResultsUseCase
    private lateinit var processRevaluationUseCase: ProcessRevaluationUseCase
    private lateinit var createSupplementaryExamUseCase: CreateSupplementaryExamUseCase
    private lateinit var createReappearExamUseCase: CreateReappearExamUseCase
    private lateinit var setReappearEligibilityUseCase: SetReappearEligibilityUseCase
    private lateinit var reportMalpracticeCaseUseCase: ReportMalpracticeCaseUseCase
    private lateinit var recordMalpracticeVerdictUseCase: RecordMalpracticeVerdictUseCase
    private lateinit var dismissMalpracticeCaseUseCase: DismissMalpracticeCaseUseCase
    private lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        getExamCentresUseCase = mockk()
        getSeatingPlansUseCase = mockk()
        getInvigilatorAssignmentsUseCase = mockk()
        getExamBlocksAdminUseCase = mockk()
        getQuestionPapersUseCase = mockk()
        getQuestionPaperSubmissionsUseCase = mockk()
        getQuestionBankUseCase = mockk()
        getExternalExaminersUseCase = mockk()
        getExternalPaperSettingsUseCase = mockk()
        getExternalEvaluationsUseCase = mockk()
        getInternalExamsUseCase = mockk()
        getInternalMarksEntriesUseCase = mockk()
        getMarksEntriesUseCase = mockk()
        getGradesUseCase = mockk()
        getEvaluationBundlesUseCase = mockk()

        addExamCentreUseCase = mockk()
        updateExamCentreUseCase = mockk()
        deleteExamCentreUseCase = mockk()
        addExamScheduleUseCase = mockk()
        updateExamScheduleUseCase = mockk()
        deleteExamScheduleUseCase = mockk()
        publishExamScheduleUseCase = mockk()
        holdExamScheduleUseCase = mockk()
        unpublishExamScheduleUseCase = mockk()
        addSeatingPlanUseCase = mockk()
        updateSeatingPlanUseCase = mockk()
        deleteSeatingPlanUseCase = mockk()
        addInvigilatorAssignmentUseCase = mockk()
        updateInvigilatorAssignmentUseCase = mockk()
        deleteInvigilatorAssignmentUseCase = mockk()
        generateHallTicketUseCase = mockk()
        updateHallTicketAdminUseCase = mockk()
        deleteHallTicketAdminUseCase = mockk()
        publishHallTicketsUseCase = mockk()
        holdHallTicketsUseCase = mockk()
        unpublishHallTicketsUseCase = mockk()
        addStudentExamBlockUseCase = mockk()
        updateStudentExamBlockUseCase = mockk()
        deleteStudentExamBlockUseCase = mockk()
        addQuestionPaperUseCase = mockk()
        updateQuestionPaperUseCase = mockk()
        deleteQuestionPaperUseCase = mockk()
        submitQuestionPaperUseCase = mockk()
        deleteQuestionPaperSubmissionUseCase = mockk()
        approveQuestionPaperUseCase = mockk()
        rejectQuestionPaperUseCase = mockk()
        addQuestionBankEntryUseCase = mockk()
        updateQuestionBankEntryUseCase = mockk()
        deleteQuestionBankEntryUseCase = mockk()
        addExternalExaminerUseCase = mockk()
        updateExternalExaminerUseCase = mockk()
        deleteExternalExaminerUseCase = mockk()
        addExternalPaperSettingUseCase = mockk()
        deleteExternalPaperSettingUseCase = mockk()
        addExternalEvaluationUseCase = mockk()
        updateExternalEvaluationUseCase = mockk()
        deleteExternalEvaluationUseCase = mockk()
        addInternalExamUseCase = mockk()
        updateInternalExamUseCase = mockk()
        deleteInternalExamUseCase = mockk()
        submitInternalMarksEntryUseCase = mockk()
        consolidateInternalMarksUseCase = mockk()
        submitMarksEntryUseCase = mockk()
        verifyMarksUseCase = mockk()
        generateGradesUseCase = mockk()
        lockGradesUseCase = mockk()
        addEvaluationBundleUseCase = mockk()
        updateEvaluationBundleUseCase = mockk()
        deleteEvaluationBundleUseCase = mockk()
        requestSecondValuationUseCase = mockk()
        applyModerationUseCase = mockk()
        generateResultsUseCase = mockk()
        approveResultsUseCase = mockk()
        publishResultsUseCase = mockk()
        holdResultPublicationUseCase = mockk()
        unpublishResultsUseCase = mockk()
        processRevaluationUseCase = mockk()
        createSupplementaryExamUseCase = mockk()
        createReappearExamUseCase = mockk()
        setReappearEligibilityUseCase = mockk()
        reportMalpracticeCaseUseCase = mockk()
        recordMalpracticeVerdictUseCase = mockk()
        dismissMalpracticeCaseUseCase = mockk()
        sessionManager = mockk()

        // Fully denied by default (matches ExamAdminPermissions' own fail-closed
        // default) - tests that care about a granted permission override this
        // explicitly. hasAnyPermission is stubbed too because hasAnyExamAdminAccess is
        // eagerly evaluated the moment the ViewModel is constructed.
        every { sessionManager.currentPermissions } returns MutableStateFlow(emptyList())
        every { sessionManager.hasPermission(any()) } returns false
        every { sessionManager.hasAnyPermission(*anyVararg()) } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): ExamAdminViewModel = ExamAdminViewModel(
        getExamCentres = getExamCentresUseCase,
        getSeatingPlans = getSeatingPlansUseCase,
        getInvigilatorAssignments = getInvigilatorAssignmentsUseCase,
        getExamBlocksAdmin = getExamBlocksAdminUseCase,
        getQuestionPapers = getQuestionPapersUseCase,
        getQuestionPaperSubmissions = getQuestionPaperSubmissionsUseCase,
        getQuestionBank = getQuestionBankUseCase,
        getExternalExaminers = getExternalExaminersUseCase,
        getExternalPaperSettings = getExternalPaperSettingsUseCase,
        getExternalEvaluations = getExternalEvaluationsUseCase,
        getInternalExams = getInternalExamsUseCase,
        getInternalMarksEntries = getInternalMarksEntriesUseCase,
        getMarksEntries = getMarksEntriesUseCase,
        getGrades = getGradesUseCase,
        getEvaluationBundles = getEvaluationBundlesUseCase,
        addExamCentreUseCase = addExamCentreUseCase,
        updateExamCentreUseCase = updateExamCentreUseCase,
        deleteExamCentreUseCase = deleteExamCentreUseCase,
        addExamScheduleUseCase = addExamScheduleUseCase,
        updateExamScheduleUseCase = updateExamScheduleUseCase,
        deleteExamScheduleUseCase = deleteExamScheduleUseCase,
        publishExamScheduleUseCase = publishExamScheduleUseCase,
        holdExamScheduleUseCase = holdExamScheduleUseCase,
        unpublishExamScheduleUseCase = unpublishExamScheduleUseCase,
        addSeatingPlanUseCase = addSeatingPlanUseCase,
        updateSeatingPlanUseCase = updateSeatingPlanUseCase,
        deleteSeatingPlanUseCase = deleteSeatingPlanUseCase,
        addInvigilatorAssignmentUseCase = addInvigilatorAssignmentUseCase,
        updateInvigilatorAssignmentUseCase = updateInvigilatorAssignmentUseCase,
        deleteInvigilatorAssignmentUseCase = deleteInvigilatorAssignmentUseCase,
        generateHallTicketUseCase = generateHallTicketUseCase,
        updateHallTicketAdminUseCase = updateHallTicketAdminUseCase,
        deleteHallTicketAdminUseCase = deleteHallTicketAdminUseCase,
        publishHallTicketsUseCase = publishHallTicketsUseCase,
        holdHallTicketsUseCase = holdHallTicketsUseCase,
        unpublishHallTicketsUseCase = unpublishHallTicketsUseCase,
        addStudentExamBlockUseCase = addStudentExamBlockUseCase,
        updateStudentExamBlockUseCase = updateStudentExamBlockUseCase,
        deleteStudentExamBlockUseCase = deleteStudentExamBlockUseCase,
        addQuestionPaperUseCase = addQuestionPaperUseCase,
        updateQuestionPaperUseCase = updateQuestionPaperUseCase,
        deleteQuestionPaperUseCase = deleteQuestionPaperUseCase,
        submitQuestionPaperUseCase = submitQuestionPaperUseCase,
        deleteQuestionPaperSubmissionUseCase = deleteQuestionPaperSubmissionUseCase,
        approveQuestionPaperUseCase = approveQuestionPaperUseCase,
        rejectQuestionPaperUseCase = rejectQuestionPaperUseCase,
        addQuestionBankEntryUseCase = addQuestionBankEntryUseCase,
        updateQuestionBankEntryUseCase = updateQuestionBankEntryUseCase,
        deleteQuestionBankEntryUseCase = deleteQuestionBankEntryUseCase,
        addExternalExaminerUseCase = addExternalExaminerUseCase,
        updateExternalExaminerUseCase = updateExternalExaminerUseCase,
        deleteExternalExaminerUseCase = deleteExternalExaminerUseCase,
        addExternalPaperSettingUseCase = addExternalPaperSettingUseCase,
        deleteExternalPaperSettingUseCase = deleteExternalPaperSettingUseCase,
        addExternalEvaluationUseCase = addExternalEvaluationUseCase,
        updateExternalEvaluationUseCase = updateExternalEvaluationUseCase,
        deleteExternalEvaluationUseCase = deleteExternalEvaluationUseCase,
        addInternalExamUseCase = addInternalExamUseCase,
        updateInternalExamUseCase = updateInternalExamUseCase,
        deleteInternalExamUseCase = deleteInternalExamUseCase,
        submitInternalMarksEntryUseCase = submitInternalMarksEntryUseCase,
        consolidateInternalMarksUseCase = consolidateInternalMarksUseCase,
        submitMarksEntryUseCase = submitMarksEntryUseCase,
        verifyMarksUseCase = verifyMarksUseCase,
        generateGradesUseCase = generateGradesUseCase,
        lockGradesUseCase = lockGradesUseCase,
        addEvaluationBundleUseCase = addEvaluationBundleUseCase,
        updateEvaluationBundleUseCase = updateEvaluationBundleUseCase,
        deleteEvaluationBundleUseCase = deleteEvaluationBundleUseCase,
        requestSecondValuationUseCase = requestSecondValuationUseCase,
        applyModerationUseCase = applyModerationUseCase,
        generateResultsUseCase = generateResultsUseCase,
        approveResultsUseCase = approveResultsUseCase,
        publishResultsUseCase = publishResultsUseCase,
        holdResultPublicationUseCase = holdResultPublicationUseCase,
        unpublishResultsUseCase = unpublishResultsUseCase,
        processRevaluationUseCase = processRevaluationUseCase,
        createSupplementaryExamUseCase = createSupplementaryExamUseCase,
        createReappearExamUseCase = createReappearExamUseCase,
        setReappearEligibilityUseCase = setReappearEligibilityUseCase,
        reportMalpracticeCaseUseCase = reportMalpracticeCaseUseCase,
        recordMalpracticeVerdictUseCase = recordMalpracticeVerdictUseCase,
        dismissMalpracticeCaseUseCase = dismissMalpracticeCaseUseCase,
        sessionManager = sessionManager
    )

    // --- ExamAdminPermissions derivation ---

    @Test
    fun `permissions default to fully denied when the session has none`() = runTest {
        val vm = viewModel()

        assertEquals(ExamAdminPermissions(), vm.permissions.value)
    }

    @Test
    fun `one granted permission string flips only its own flag`() = runTest {
        val addExamCentrePermission = requiredPermissionFor(
            ExamEndpoint.MODULE, ExamEndpoint.SubModules.EXAM_CENTER, ExamEndpoint.Actions.EXAM_CENTRE, ExamEndpoint.ActionTypes.ADD
        )
        every { sessionManager.hasPermission(addExamCentrePermission) } returns true
        every { sessionManager.hasPermission(match { it != addExamCentrePermission }) } returns false

        val vm = viewModel()

        val permissions = vm.permissions.value
        assertTrue(permissions.canAddExamCentre)
        assertFalse(permissions.canUpdateExamCentre)
        assertFalse(permissions.canDeleteExamCentre)
        assertFalse(permissions.canAddExamSchedule)
        assertFalse(permissions.canApproveResults)
    }

    @Test
    fun `hasAnyExamAdminAccess is false when nothing is granted`() = runTest {
        val vm = viewModel()

        assertFalse(vm.hasAnyExamAdminAccess.value)
    }

    @Test
    fun `hasAnyExamAdminAccess is true the moment any one admin action is granted`() = runTest {
        every { sessionManager.hasAnyPermission(*anyVararg()) } returns true

        val vm = viewModel()

        assertTrue(vm.hasAnyExamAdminAccess.value)
    }

    // --- Submit-then-reload write actions ---

    @Test
    fun `addExamCentre invokes the use case then reloads exam centres`() = runTest {
        coEvery { getExamCentresUseCase() } returns emptyList()
        coEvery { addExamCentreUseCase("Central Examination Hall", "250", null, listOf("101")) } just Runs

        val vm = viewModel()
        vm.loadExamCentres()

        vm.effect.test {
            vm.addExamCentre("Central Examination Hall", "250", null, listOf("101"))
            assertEquals(ExamEffect.ShowToast("Exam centre added"), awaitItem())
        }
        coVerify(exactly = 1) { addExamCentreUseCase("Central Examination Hall", "250", null, listOf("101")) }
        coVerify(exactly = 2) { getExamCentresUseCase() } // initial load + post-add reload
    }

    @Test
    fun `deleteSeatingPlan invokes the use case then reloads seating plans`() = runTest {
        coEvery { getSeatingPlansUseCase(null) } returns emptyList()
        coEvery { getInvigilatorAssignmentsUseCase(null) } returns emptyList()
        coEvery { deleteSeatingPlanUseCase("12") } just Runs

        val vm = viewModel()
        vm.loadSeatingAndInvigilation()

        vm.deleteSeatingPlan("12")

        coVerify(exactly = 1) { deleteSeatingPlanUseCase("12") }
        coVerify(exactly = 2) { getSeatingPlansUseCase(null) } // initial load + post-delete reload
    }

    @Test
    fun `publishExamSchedule emits a toast but reloads nothing - there is no schedule section in this ViewModel`() = runTest {
        coEvery { publishExamScheduleUseCase("10") } just Runs

        val vm = viewModel()

        vm.effect.test {
            vm.publishExamSchedule("10")
            assertEquals(ExamEffect.ShowToast("Exam schedule published"), awaitItem())
        }
        coVerify(exactly = 1) { publishExamScheduleUseCase("10") }
    }

    @Test
    fun `approveResults emits a success toast and never reloads a section`() = runTest {
        coEvery { approveResultsUseCase("45", "ADMIN", "Level 1 approval") } returns
            ResultApprovalStatus("45", "ADMIN", false, "Level 1 approval")

        val vm = viewModel()

        vm.effect.test {
            vm.approveResults("45", "ADMIN", "Level 1 approval")
            assertEquals(ExamEffect.ShowToast("Results approved"), awaitItem())
        }
        coVerify(exactly = 1) { approveResultsUseCase("45", "ADMIN", "Level 1 approval") }
    }

    // --- Error handling ---

    @Test
    fun `a failed write emits the business error message instead of a generic one`() = runTest {
        coEvery { approveResultsUseCase("45", "ADMIN", null) } throws
            APIError.BusinessError("CHAIN_BROKEN", "Previous approval level is missing")

        val vm = viewModel()

        vm.effect.test {
            vm.approveResults("45", "ADMIN")
            assertEquals(ExamEffect.ShowToast("Previous approval level is missing"), awaitItem())
        }
    }
}
