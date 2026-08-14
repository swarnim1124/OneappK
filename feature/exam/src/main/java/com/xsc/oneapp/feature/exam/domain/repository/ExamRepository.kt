package com.xsc.oneapp.feature.exam.domain.repository

import com.xsc.oneapp.feature.exam.domain.model.ChallengeRevaluation
import com.xsc.oneapp.feature.exam.domain.model.EvaluationBundle
import com.xsc.oneapp.feature.exam.domain.model.ExamAppeal
import com.xsc.oneapp.feature.exam.domain.model.ExamCentre
import com.xsc.oneapp.feature.exam.domain.model.ExamResult
import com.xsc.oneapp.feature.exam.domain.model.ExamSchedule
import com.xsc.oneapp.feature.exam.domain.model.ExternalEvaluation
import com.xsc.oneapp.feature.exam.domain.model.ExternalExaminer
import com.xsc.oneapp.feature.exam.domain.model.ExternalPaperSetting
import com.xsc.oneapp.feature.exam.domain.model.Grade
import com.xsc.oneapp.feature.exam.domain.model.HallTicket
import com.xsc.oneapp.feature.exam.domain.model.InternalExam
import com.xsc.oneapp.feature.exam.domain.model.InvigilatorAssignment
import com.xsc.oneapp.feature.exam.domain.model.MalpracticeCase
import com.xsc.oneapp.feature.exam.domain.model.MarksRecord
import com.xsc.oneapp.feature.exam.domain.model.PhotocopyRequest
import com.xsc.oneapp.feature.exam.domain.model.QuestionBankEntry
import com.xsc.oneapp.feature.exam.domain.model.QuestionPaper
import com.xsc.oneapp.feature.exam.domain.model.QuestionPaperSubmission
import com.xsc.oneapp.feature.exam.domain.model.ReappearEligibility
import com.xsc.oneapp.feature.exam.domain.model.ReappearExam
import com.xsc.oneapp.feature.exam.domain.model.ResultApprovalStatus
import com.xsc.oneapp.feature.exam.domain.model.RevaluationRequest
import com.xsc.oneapp.feature.exam.domain.model.SeatingPlan
import com.xsc.oneapp.feature.exam.domain.model.StudentExamBlock
import com.xsc.oneapp.feature.exam.domain.model.SupplementaryExam

/**
 * The full m_exam contract: both the student-facing routes and the
 * examination-administration routes (marks entry, grading, evaluation, approval
 * chains, invigilation, question papers). Admin methods are not gated here - the UI
 * layer gates them per-action via `ExamAdminPermissions`/`SessionManager.hasPermission`,
 * the same way `FeeRepository`'s admin methods are gated by `FeePermissions`. See
 * [com.xsc.oneapp.feature.exam.data.network.ExamEndpoint] for the full route table.
 */
interface ExamRepository {
    suspend fun getExamSchedules(): List<ExamSchedule>
    suspend fun getHallTicket(scheduleId: String): List<HallTicket>
    suspend fun getMyResults(): List<ExamResult>

    /** Examination holds on the signed-in student, optionally narrowed to one schedule. */
    suspend fun getMyExamBlocks(scheduleId: String? = null): List<StudentExamBlock>

    suspend fun getMyRevaluationRequests(): List<RevaluationRequest>
    suspend fun submitRevaluationRequest(scheduleId: String, courseId: String, reason: String?)

    suspend fun getMyPhotocopyRequests(): List<PhotocopyRequest>
    suspend fun submitPhotocopyRequest(scheduleId: String, courseId: String)

    suspend fun getMyAppeals(): List<ExamAppeal>
    suspend fun submitAppeal(referenceId: String, reason: String)

    suspend fun getMyChallengeRevaluations(): List<ChallengeRevaluation>
    suspend fun submitChallengeRevaluation(
        scheduleId: String,
        courseId: String,
        revaluationRequestId: String
    )

    suspend fun getSupplementaryExams(): List<SupplementaryExam>
    suspend fun registerForSupplementaryExam(supplementaryExamId: String, courseIds: List<String>)

    suspend fun getReappearExams(): List<ReappearExam>
    suspend fun registerForReappearExam(reappearExamId: String, courseIds: List<String>)
    suspend fun getMyReappearEligibility(reappearExamId: String?): List<ReappearEligibility>

    suspend fun getMyMalpracticeCases(): List<MalpracticeCase>

    // ---- Admin: sm_examCenter ----
    suspend fun getExamCentres(): List<ExamCentre>
    suspend fun addExamCentre(centreName: String, capacity: String, address: String?, courses: List<String>)
    suspend fun updateExamCentre(
        centreId: String,
        centreName: String?,
        capacity: String?,
        address: String?,
        courses: List<String>?,
        status: String?
    )
    suspend fun deleteExamCentre(centreId: String)

    // ---- Admin: sm_schedule ----
    suspend fun addExamSchedule(
        name: String,
        examType: String,
        fromDate: String,
        toDate: String,
        sessionDate: String,
        session: String
    )
    suspend fun updateExamSchedule(scheduleId: String, name: String?, fromDate: String?, toDate: String?)
    suspend fun deleteExamSchedule(scheduleId: String)
    suspend fun publishExamSchedule(scheduleId: String)
    suspend fun holdExamSchedule(scheduleId: String)
    suspend fun unpublishExamSchedule(scheduleId: String)

    // ---- Admin: sm_seating ----
    suspend fun getSeatingPlans(scheduleId: String? = null): List<SeatingPlan>
    suspend fun addSeatingPlan(
        scheduleId: String,
        venueId: String,
        courseId: String,
        studentId: String,
        seatNumber: String,
        allocationMethod: String?
    )
    suspend fun updateSeatingPlan(id: String, seatNumber: String?, venueId: String?)
    suspend fun deleteSeatingPlan(id: String)

    // ---- Admin: sm_invigilation ----
    suspend fun getInvigilatorAssignments(scheduleId: String? = null): List<InvigilatorAssignment>
    suspend fun addInvigilatorAssignment(
        scheduleId: String,
        venueId: String,
        facultyId: String,
        dutyType: String
    )
    suspend fun updateInvigilatorAssignment(id: String, dutyType: String?, status: String?)
    suspend fun deleteInvigilatorAssignment(id: String)

    // ---- Admin: sm_hallTicket ----
    suspend fun generateHallTicket(
        scheduleId: String,
        studentId: String,
        venueDetails: String?,
        seatNumber: String?
    )
    suspend fun updateHallTicketAdmin(id: String, venueDetails: String?, seatNumber: String?)
    suspend fun deleteHallTicketAdmin(id: String)
    suspend fun publishHallTickets(scheduleId: String)
    suspend fun holdHallTickets(scheduleId: String)
    suspend fun unpublishHallTickets(scheduleId: String)

    /** Admin view of exam blocks across students, optionally narrowed to one schedule -
     * distinct from [getMyExamBlocks], which is always scoped to the signed-in student. */
    suspend fun getExamBlocksAdmin(scheduleId: String? = null): List<StudentExamBlock>
    suspend fun addStudentExamBlock(studentId: String, scheduleId: String, reason: String)
    suspend fun updateStudentExamBlock(id: String, reason: String?, status: String?)
    suspend fun deleteStudentExamBlock(id: String)

    // ---- Admin: sm_questionPaper ----
    suspend fun getQuestionPapers(scheduleId: String? = null): List<QuestionPaper>
    suspend fun addQuestionPaper(scheduleId: String, courseId: String, content: String)
    suspend fun updateQuestionPaper(id: String, content: String?, status: String?)
    suspend fun deleteQuestionPaper(id: String)

    suspend fun getQuestionPaperSubmissions(questionPaperId: String? = null): List<QuestionPaperSubmission>
    suspend fun submitQuestionPaper(questionPaperId: String)
    suspend fun deleteQuestionPaperSubmission(id: String)

    suspend fun approveQuestionPaper(questionPaperId: String)
    suspend fun rejectQuestionPaper(questionPaperId: String, reason: String?)

    suspend fun getQuestionBank(courseId: String? = null): List<QuestionBankEntry>
    suspend fun addQuestionBankEntry(courseId: String, content: String, questionPaperId: String?)
    suspend fun updateQuestionBankEntry(id: String, content: String?)
    suspend fun deleteQuestionBankEntry(id: String)

    // ---- Admin: sm_externalExam ----
    suspend fun getExternalExaminers(): List<ExternalExaminer>
    suspend fun addExternalExaminer(
        examinerName: String,
        contactEmail: String?,
        contactPhone: String?,
        specialization: String?
    )
    suspend fun updateExternalExaminer(
        id: String,
        examinerName: String?,
        contactEmail: String?,
        contactPhone: String?,
        specialization: String?
    )
    suspend fun deleteExternalExaminer(id: String)

    suspend fun getExternalPaperSettings(): List<ExternalPaperSetting>
    suspend fun addExternalPaperSetting(examinerId: String, courseId: String, scheduleId: String)
    suspend fun deleteExternalPaperSetting(id: String)

    suspend fun getExternalEvaluations(): List<ExternalEvaluation>
    suspend fun addExternalEvaluation(
        examinerId: String,
        courseId: String,
        scheduleId: String,
        scriptBundleIds: List<String>
    )
    suspend fun updateExternalEvaluation(id: String, status: String?)
    suspend fun deleteExternalEvaluation(id: String)

    // ---- Admin: sm_internalExam ----
    suspend fun getInternalExams(courseOfferingId: String? = null): List<InternalExam>
    suspend fun addInternalExam(
        courseOfferingId: String,
        examName: String,
        examTypeId: String,
        examDate: String,
        totalMaxMarks: String
    )
    suspend fun updateInternalExam(
        id: String,
        examName: String?,
        examDate: String?,
        totalMaxMarks: String?,
        status: String?
    )
    suspend fun deleteInternalExam(id: String)

    suspend fun getInternalMarksEntries(internalExamId: String? = null): List<MarksRecord>
    suspend fun submitInternalMarksEntry(internalExamId: String, records: List<Pair<String, String>>)
    suspend fun consolidateInternalMarks(courseOfferingId: String)

    // ---- Admin: sm_marks ----
    suspend fun getMarksEntries(scheduleId: String? = null, courseId: String? = null): List<MarksRecord>
    suspend fun submitMarksEntry(scheduleId: String, courseId: String, records: List<Pair<String, String>>)
    suspend fun verifyMarks(scheduleId: String, courseId: String)

    suspend fun getGrades(scheduleId: String? = null): List<Grade>
    suspend fun generateGrades(scheduleId: String, courseId: String)
    suspend fun lockGrades(scheduleId: String, courseId: String)

    // ---- Admin: sm_evaluation ----
    suspend fun getEvaluationBundles(scheduleId: String? = null): List<EvaluationBundle>
    suspend fun addEvaluationBundle(
        scheduleId: String,
        courseId: String,
        evaluatorId: String,
        studentIds: List<String>
    )
    suspend fun updateEvaluationBundle(id: String, status: String?)
    suspend fun deleteEvaluationBundle(id: String)
    suspend fun requestSecondValuation(scheduleId: String, courseId: String, studentId: String)
    suspend fun applyModeration(scheduleId: String, courseId: String, adjustment: String)

    // ---- Admin: sm_results / sm_resultApproval ----
    suspend fun generateResults(scheduleId: String)
    suspend fun approveResults(scheduleId: String, level: String, remarks: String?): ResultApprovalStatus
    suspend fun rejectResults(scheduleId: String, level: String, remarks: String?)
    suspend fun publishResults(scheduleId: String)
    suspend fun holdResultPublication(scheduleId: String)
    suspend fun unpublishResults(scheduleId: String)

    // ---- Admin actions folded into existing student screens ----

    /** sm_revaluation/revaluationProcessing - assigns a re-evaluator to a filed request. */
    suspend fun processRevaluation(revaluationRequestId: String, evaluatorId: String)

    /** sm_supplementary/supplementaryExam/add - schedules the session itself. */
    suspend fun createSupplementaryExam(
        name: String,
        fromDate: String,
        toDate: String,
        courseIds: List<String>
    )

    /** sm_reappear/reappearExam/add - schedules the session itself. */
    suspend fun createReappearExam(
        name: String,
        academicTermId: String,
        examStartDate: String,
        examEndDate: String,
        registrationStartDate: String,
        registrationEndDate: String,
        courseIds: List<String>
    )

    /** sm_reappear/reappearEligibility/add - sets the eligibility rule for a student/course. */
    suspend fun setReappearEligibility(
        studentId: String,
        courseId: String,
        reappearExamId: String,
        isEligible: Boolean,
        reason: String?
    )

    /** sm_malpractice/malpracticeCase/add - reports an incident (staff action). */
    suspend fun reportMalpracticeCase(studentId: String, scheduleId: String, description: String)

    /** sm_malpractice/malpracticeCase/update - records a committee verdict. */
    suspend fun recordMalpracticeVerdict(
        caseId: String,
        committee: String?,
        hearingDate: String?,
        verdict: String
    )

    /** sm_malpractice/malpracticeCase/delete - dismisses a case. */
    suspend fun dismissMalpracticeCase(caseId: String)
}
