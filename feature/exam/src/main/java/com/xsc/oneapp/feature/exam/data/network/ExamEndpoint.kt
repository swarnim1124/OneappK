package com.xsc.oneapp.feature.exam.data.network

/**
 * Mirrors the m_exam API contract v1.2 (2026-08-13) §2 "Canonical Route Index" in
 * full - all 16 submodules and 48 actions - so any later screen picks its route from
 * here rather than re-deriving it from the contract document.
 *
 * Only the student-facing subset is wired into [com.xsc.oneapp.feature.exam.data.repository.ExamRepositoryImpl]:
 * schedules, hall tickets + exam blocks, results, the revaluation family, supplementary
 * and reappear registration, and malpractice cases the student is named in. The
 * remaining routes are examination-administration surfaces (centre CRUD, schedule
 * authoring and publication, seating generation, invigilator duty rosters, question
 * paper drafting/approval, external examiner registry, internal and end-semester marks
 * entry, grading, evaluation bundles, moderation, result calculation and the approval
 * chain, malpractice hearings). This app has no staff console to host them, so they are
 * catalogued here rather than given dead repository methods no screen can reach.
 *
 * Submodule naming: the contract lists camelCase and snake_case aliases for several
 * submodules (`sm_examCenter` / `sm_exam_center`). The camelCase spellings below are
 * the ones verified against the live gateway (2026-07-30/31); the aliases are noted in
 * the contract but not duplicated here since sending both is not possible anyway.
 */
object ExamEndpoint {
    const val MODULE = "m_exam"

    object SubModules {
        const val EXAM_CENTER = "sm_examCenter"
        const val SCHEDULE = "sm_schedule"
        const val SEATING = "sm_seating"
        const val INVIGILATION = "sm_invigilation"
        const val HALL_TICKET = "sm_hallTicket"
        const val QUESTION_PAPER = "sm_questionPaper"
        const val EXTERNAL_EXAM = "sm_externalExam"
        const val INTERNAL_EXAM = "sm_internalExam"
        const val MARKS = "sm_marks"
        const val EVALUATION = "sm_evaluation"
        const val RESULTS = "sm_results"
        const val RESULT_APPROVAL = "sm_resultApproval"
        const val REVALUATION = "sm_revaluation"
        const val SUPPLEMENTARY = "sm_supplementary"
        const val REAPPEAR = "sm_reappear"
        const val MALPRACTICE = "sm_malpractice"
    }

    object Actions {
        // sm_examCenter
        const val EXAM_CENTRE = "examCentre"

        // sm_schedule
        const val EXAM_SCHEDULE = "examSchedule"
        const val EXAM_SCHEDULE_PUBLICATION = "examSchedulePublication"

        // sm_seating
        const val SEATING_PLAN = "seatingPlan"

        // sm_invigilation
        const val INVIGILATOR_ASSIGNMENT = "invigilatorAssignment"

        // sm_hallTicket
        const val HALL_TICKET = "hallTicket"
        const val HALL_TICKET_PUBLICATION = "hallTicketPublication"
        const val STUDENT_BLOCK = "studentBlock"

        // sm_questionPaper
        const val QUESTION_PAPER = "questionPaper"
        const val QUESTION_PAPER_SUBMISSION = "questionPaperSubmission"
        const val QUESTION_PAPER_APPROVAL = "questionPaperApproval"
        const val QUESTION_BANK = "questionBank"

        // sm_externalExam
        const val EXTERNAL_EXAMINER = "externalExaminer"
        const val EXTERNAL_PAPER_SETTING = "externalPaperSetting"
        const val EXTERNAL_EVALUATION = "externalEvaluation"

        // sm_internalExam
        const val INTERNAL_EXAM = "internalExam"
        const val INTERNAL_MARKS_ENTRY = "internalMarksEntry"
        const val INTERNAL_MARKS_CONSOLIDATION = "internalMarksConsolidation"

        // sm_marks
        const val MARKS_ENTRY = "marksEntry"
        const val MARKS_VERIFICATION = "marksVerification"
        const val GRADE = "grade"
        const val GRADE_LOCK = "gradeLock"

        // sm_evaluation
        const val EVALUATION_BUNDLE = "evaluationBundle"
        const val SECOND_VALUATION = "secondValuation"
        const val MODERATION = "moderation"

        // sm_results
        const val RESULT = "result"

        // sm_resultApproval
        const val RESULT_APPROVAL = "resultApproval"
        const val RESULT_PUBLICATION = "resultPublication"

        // sm_revaluation
        const val REVALUATION_REQUEST = "revaluationRequest"
        const val REVALUATION_PROCESSING = "revaluationProcessing"
        const val PHOTOCOPY_REQUEST = "photocopyRequest"
        const val APPEAL = "appeal"
        const val CHALLENGE_REVALUATION = "challengeRevaluation"

        // sm_supplementary
        const val SUPPLEMENTARY_EXAM = "supplementaryExam"
        const val SUPPLEMENTARY_REGISTRATION = "supplementaryRegistration"

        // sm_reappear
        const val REAPPEAR_EXAM = "reappearExam"
        const val REAPPEAR_REGISTRATION = "reappearRegistration"
        const val REAPPEAR_ELIGIBILITY = "reappearEligibility"

        // sm_malpractice
        const val MALPRACTICE_CASE = "malpracticeCase"
    }

    /**
     * §1: `add`/`view`/`update`/`delete` are the CRUD verbs every action accepts. The
     * remainder are the action-specific workflow verbs the contract documents as
     * aliases - e.g. schedule publication accepts `publish`/`hold`/`unpublish` as
     * synonyms for `add`/`update`/`delete`. The CRUD spellings are what this module
     * sends, since they are accepted everywhere and the aliases are not.
     */
    object ActionTypes {
        const val ADD = "add"
        const val VIEW = "view"
        const val UPDATE = "update"
        const val DELETE = "delete"

        const val PUBLISH = "publish"
        const val HOLD = "hold"
        const val UNPUBLISH = "unpublish"
        const val APPROVE = "approve"
        const val REJECT = "reject"
        const val REPORT = "report"
        const val DISMISS = "dismiss"
    }
}
