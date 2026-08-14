package com.xsc.oneapp.feature.exam.ui.state

/**
 * Per-action gating for every examination-administration surface - the same
 * `FeePermissions`-style contract: each field decides whether to *show* an action,
 * never whether a request is safe to *send*; the backend remains the only real gate.
 * Defaults to fully denied so a session that hasn't resolved its permissions yet (or
 * a student session with none) never briefly flashes an admin control.
 *
 * Publish/hold/unpublish trios and approve/reject pairs share one flag each - they are
 * the same admin capability exercised in different directions, exactly like
 * `FeePermissions.canApproveRefund` already covers both approving and rejecting a
 * refund from one grant.
 */
data class ExamAdminPermissions(
    // sm_examCenter
    val canAddExamCentre: Boolean = false,
    val canUpdateExamCentre: Boolean = false,
    val canDeleteExamCentre: Boolean = false,

    // sm_schedule
    val canAddExamSchedule: Boolean = false,
    val canUpdateExamSchedule: Boolean = false,
    val canDeleteExamSchedule: Boolean = false,
    val canPublishExamSchedule: Boolean = false,

    // sm_seating
    val canAddSeatingPlan: Boolean = false,
    val canUpdateSeatingPlan: Boolean = false,
    val canDeleteSeatingPlan: Boolean = false,

    // sm_invigilation
    val canAddInvigilatorAssignment: Boolean = false,
    val canUpdateInvigilatorAssignment: Boolean = false,
    val canDeleteInvigilatorAssignment: Boolean = false,

    // sm_hallTicket (admin)
    val canGenerateHallTicket: Boolean = false,
    val canUpdateHallTicketAdmin: Boolean = false,
    val canDeleteHallTicketAdmin: Boolean = false,
    val canPublishHallTickets: Boolean = false,
    val canAddStudentExamBlock: Boolean = false,
    val canUpdateStudentExamBlock: Boolean = false,
    val canDeleteStudentExamBlock: Boolean = false,

    // sm_questionPaper
    val canAddQuestionPaper: Boolean = false,
    val canUpdateQuestionPaper: Boolean = false,
    val canDeleteQuestionPaper: Boolean = false,
    val canSubmitQuestionPaper: Boolean = false,
    val canDeleteQuestionPaperSubmission: Boolean = false,
    val canApproveQuestionPaper: Boolean = false,
    val canAddQuestionBankEntry: Boolean = false,
    val canUpdateQuestionBankEntry: Boolean = false,
    val canDeleteQuestionBankEntry: Boolean = false,

    // sm_externalExam
    val canAddExternalExaminer: Boolean = false,
    val canUpdateExternalExaminer: Boolean = false,
    val canDeleteExternalExaminer: Boolean = false,
    val canAddExternalPaperSetting: Boolean = false,
    val canDeleteExternalPaperSetting: Boolean = false,
    val canAddExternalEvaluation: Boolean = false,
    val canUpdateExternalEvaluation: Boolean = false,
    val canDeleteExternalEvaluation: Boolean = false,

    // sm_internalExam
    val canAddInternalExam: Boolean = false,
    val canUpdateInternalExam: Boolean = false,
    val canDeleteInternalExam: Boolean = false,
    val canSubmitInternalMarksEntry: Boolean = false,
    val canConsolidateInternalMarks: Boolean = false,

    // sm_marks
    val canSubmitMarksEntry: Boolean = false,
    val canVerifyMarks: Boolean = false,
    val canGenerateGrades: Boolean = false,
    val canLockGrades: Boolean = false,

    // sm_evaluation
    val canAddEvaluationBundle: Boolean = false,
    val canUpdateEvaluationBundle: Boolean = false,
    val canDeleteEvaluationBundle: Boolean = false,
    val canRequestSecondValuation: Boolean = false,
    val canApplyModeration: Boolean = false,

    // sm_results / sm_resultApproval
    val canGenerateResults: Boolean = false,
    val canApproveResults: Boolean = false,
    val canPublishResults: Boolean = false,

    // Folded into existing student screens
    val canProcessRevaluation: Boolean = false,
    val canCreateSupplementaryExam: Boolean = false,
    val canCreateReappearExam: Boolean = false,
    val canSetReappearEligibility: Boolean = false,
    val canReportMalpracticeCase: Boolean = false,
    val canRecordMalpracticeVerdict: Boolean = false,
    val canDismissMalpracticeCase: Boolean = false
)
