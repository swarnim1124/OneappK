package com.xsc.oneapp.feature.exam.data.mapper

import com.google.gson.JsonObject
import com.xsc.oneapp.core.json.JsonRowUtils
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
 * m_exam contract rows are schema-less (field names may be snake_case and vary by
 * table), so this reads defensively via JsonRowUtils instead of a rigid
 * @SerializedName DTO - same tolerance the UI used to apply itself, just moved out
 * of the Composable. examSchedule's field names verified against a real response
 * example (2026-07-30).
 */
fun JsonObject.toExamSchedule(): ExamSchedule = ExamSchedule(
    id = JsonRowUtils.firstString(this, "id"),
    name = JsonRowUtils.firstString(this, "exam_name", "examName") ?: "Exam Schedule",
    fromDate = JsonRowUtils.firstString(this, "from_date", "fromDate"),
    toDate = JsonRowUtils.firstString(this, "to_date", "toDate"),
    status = JsonRowUtils.firstString(this, "status"),
    examType = JsonRowUtils.firstString(this, "exam_type", "examType")
)

/** hallTicket/view field names verified against a real response example (2026-07-30). */
fun JsonObject.toHallTicket(): HallTicket = HallTicket(
    id = JsonRowUtils.firstString(this, "id"),
    scheduleId = JsonRowUtils.firstString(this, "schedule_id", "scheduleId"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studId"),
    venueDetails = JsonRowUtils.firstString(this, "venue_details", "venueDetails"),
    seatNumber = JsonRowUtils.firstString(this, "seat_number", "seatNumber"),
    status = JsonRowUtils.firstString(this, "status")
)

/** sm_results/result/view field names verified against a real response example
 * (2026-07-31) - aggregate GPA/CGPA per schedule, see ExamResult's doc comment. */
fun JsonObject.toExamResult(): ExamResult = ExamResult(
    id = JsonRowUtils.firstString(this, "id"),
    scheduleId = JsonRowUtils.firstString(this, "schedule_id", "scheduleId"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studId"),
    gpa = JsonRowUtils.firstString(this, "gpa"),
    cgpa = JsonRowUtils.firstString(this, "cgpa"),
    resultStatus = JsonRowUtils.firstString(this, "result_status", "resultStatus"),
    createdAt = JsonRowUtils.firstString(this, "created_at", "createdAt")
)

/** sm_revaluation/revaluationRequest/view field names verified against a real
 * response example (2026-07-31). */
fun JsonObject.toRevaluationRequest(): RevaluationRequest = RevaluationRequest(
    id = JsonRowUtils.firstString(this, "id"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studId"),
    courseId = JsonRowUtils.firstString(this, "course_id", "courseId"),
    scheduleId = JsonRowUtils.firstString(this, "schedule_id", "scheduleId"),
    reason = JsonRowUtils.firstString(this, "reason"),
    status = JsonRowUtils.firstString(this, "status"),
    createdAt = JsonRowUtils.firstString(this, "created_at", "createdAt")
)

/** sm_revaluation/challengeRevaluation/view field names verified against a real
 * response example (2026-07-31). */
fun JsonObject.toChallengeRevaluation(): ChallengeRevaluation = ChallengeRevaluation(
    id = JsonRowUtils.firstString(this, "id"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studId"),
    courseId = JsonRowUtils.firstString(this, "course_id", "courseId"),
    scheduleId = JsonRowUtils.firstString(this, "schedule_id", "scheduleId"),
    revalRequestId = JsonRowUtils.firstString(this, "reval_request_id", "revalRequestId"),
    reason = JsonRowUtils.firstString(this, "reason"),
    status = JsonRowUtils.firstString(this, "status"),
    createdAt = JsonRowUtils.firstString(this, "created_at", "createdAt")
)

/** sm_hallTicket/studentBlock/view. The contract (§4.3) documents the request payload
 * but not a response row shape, so every field is read through the usual
 * snake_case/camelCase candidate list and stays nullable. */
fun JsonObject.toStudentExamBlock(): StudentExamBlock = StudentExamBlock(
    id = JsonRowUtils.firstString(this, "id", "block_id", "blockId"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studId", "student_id", "studentId"),
    scheduleId = JsonRowUtils.firstString(this, "schedule_id", "scheduleId"),
    reason = JsonRowUtils.firstString(this, "reason"),
    blockedBy = JsonRowUtils.firstString(this, "blocked_by", "blockedBy"),
    status = JsonRowUtils.firstString(this, "status"),
    createdAt = JsonRowUtils.firstString(this, "created_at", "createdAt")
)

/** sm_revaluation/photocopyRequest/view. */
fun JsonObject.toPhotocopyRequest(): PhotocopyRequest = PhotocopyRequest(
    id = JsonRowUtils.firstString(this, "id"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studId", "student_id", "studentId"),
    scheduleId = JsonRowUtils.firstString(this, "schedule_id", "scheduleId"),
    courseId = JsonRowUtils.firstString(this, "course_id", "courseId"),
    status = JsonRowUtils.firstString(this, "status"),
    createdAt = JsonRowUtils.firstString(this, "created_at", "createdAt")
)

/** sm_revaluation/appeal/view. */
fun JsonObject.toExamAppeal(): ExamAppeal = ExamAppeal(
    id = JsonRowUtils.firstString(this, "id"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studId", "student_id", "studentId"),
    referenceId = JsonRowUtils.firstString(this, "reference_id", "referenceId"),
    reason = JsonRowUtils.firstString(this, "reason"),
    status = JsonRowUtils.firstString(this, "status"),
    createdAt = JsonRowUtils.firstString(this, "created_at", "createdAt")
)

/** sm_supplementary/supplementaryExam/view. */
fun JsonObject.toSupplementaryExam(): SupplementaryExam = SupplementaryExam(
    id = JsonRowUtils.firstString(this, "id"),
    name = JsonRowUtils.firstString(this, "exam_name", "examName", "name"),
    fromDate = JsonRowUtils.firstString(this, "from_date", "fromDate"),
    toDate = JsonRowUtils.firstString(this, "to_date", "toDate"),
    status = JsonRowUtils.firstString(this, "status"),
    courses = JsonRowUtils.firstStringArray(this, "courses", "course_ids", "courseIds")
)

/** sm_reappear/reappearExam/view. */
fun JsonObject.toReappearExam(): ReappearExam = ReappearExam(
    id = JsonRowUtils.firstString(this, "id"),
    name = JsonRowUtils.firstString(this, "exam_name", "examName", "name"),
    academicTermId = JsonRowUtils.firstString(this, "academic_term_id", "academicTermId"),
    examStartDate = JsonRowUtils.firstString(this, "exam_start_date", "examStartDate"),
    examEndDate = JsonRowUtils.firstString(this, "exam_end_date", "examEndDate"),
    registrationStartDate = JsonRowUtils.firstString(
        this, "registration_start_date", "registrationStartDate"
    ),
    registrationEndDate = JsonRowUtils.firstString(
        this, "registration_end_date", "registrationEndDate"
    ),
    status = JsonRowUtils.firstString(this, "status"),
    courses = JsonRowUtils.firstStringArray(this, "courses", "course_ids", "courseIds")
)

/** sm_reappear/reappearEligibility/view. The contract documents this route as
 * rule-setting and fixes no response shape, so the eligibility flag is read from any of
 * the plausible spellings and left null when the row carries none - a null renders as
 * "not determined" rather than being coerced to false, which would wrongly tell a
 * student they are barred. */
fun JsonObject.toReappearEligibility(): ReappearEligibility = ReappearEligibility(
    id = JsonRowUtils.firstString(this, "id"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studId", "student_id", "studentId"),
    courseId = JsonRowUtils.firstString(this, "course_id", "courseId"),
    reappearExamId = JsonRowUtils.firstString(this, "reappear_exam_id", "reappearExamId"),
    isEligible = JsonRowUtils.firstString(
        this, "is_eligible", "isEligible", "eligible"
    )?.toBooleanFlag(),
    reason = JsonRowUtils.firstString(this, "reason", "remarks"),
    status = JsonRowUtils.firstString(this, "status")
)

/** sm_malpractice/malpracticeCase/view. */
fun JsonObject.toMalpracticeCase(): MalpracticeCase = MalpracticeCase(
    id = JsonRowUtils.firstString(this, "id", "case_id", "caseId"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studId", "student_id", "studentId"),
    scheduleId = JsonRowUtils.firstString(this, "schedule_id", "scheduleId"),
    description = JsonRowUtils.firstString(this, "description"),
    reportedBy = JsonRowUtils.firstString(this, "reported_by", "reportedBy"),
    committee = JsonRowUtils.firstString(this, "committee"),
    hearingDate = JsonRowUtils.firstString(this, "hearing_date", "hearingDate"),
    verdict = JsonRowUtils.firstString(this, "verdict"),
    status = JsonRowUtils.firstString(this, "status"),
    createdAt = JsonRowUtils.firstString(this, "created_at", "createdAt")
)

/** These rows reach us as a JSON boolean, as 1/0, or as a "true"/"yes" string
 * depending on the table, and JsonRowUtils flattens all three to a string. Anything
 * unrecognized stays null rather than defaulting either way. */
private fun String.toBooleanFlag(): Boolean? = when (lowercase()) {
    "true", "1", "yes", "y" -> true
    "false", "0", "no", "n" -> false
    else -> null
}

/** sm_examCenter/examCentre/view. */
fun JsonObject.toExamCentre(): ExamCentre = ExamCentre(
    id = JsonRowUtils.firstString(this, "id", "centre_id", "centreId"),
    centreName = JsonRowUtils.firstString(this, "centre_name", "centreName"),
    capacity = JsonRowUtils.firstString(this, "capacity"),
    address = JsonRowUtils.firstString(this, "address"),
    courses = JsonRowUtils.firstStringArray(this, "courses", "course_ids", "courseIds"),
    status = JsonRowUtils.firstString(this, "status")
)

/** sm_seating/seatingPlan/view. Admin-only, no confirmed response example - field
 * names inferred from the add payload's own vocabulary. */
fun JsonObject.toSeatingPlan(): SeatingPlan = SeatingPlan(
    id = JsonRowUtils.firstString(this, "id"),
    scheduleId = JsonRowUtils.firstString(this, "schedule_id", "scheduleId"),
    venueId = JsonRowUtils.firstString(this, "venue_id", "venueId"),
    courseId = JsonRowUtils.firstString(this, "course_id", "courseId"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studId", "student_id", "studentId"),
    seatNumber = JsonRowUtils.firstString(this, "seat_number", "seatNumber"),
    allocationMethod = JsonRowUtils.firstString(this, "allocation_method", "allocationMethod")
)

/** sm_invigilation/invigilatorAssignment/view. Admin-only, field names inferred from
 * the add payload's own vocabulary. */
fun JsonObject.toInvigilatorAssignment(): InvigilatorAssignment = InvigilatorAssignment(
    id = JsonRowUtils.firstString(this, "id"),
    scheduleId = JsonRowUtils.firstString(this, "schedule_id", "scheduleId"),
    venueId = JsonRowUtils.firstString(this, "venue_id", "venueId"),
    facultyId = JsonRowUtils.firstString(this, "faculty_id", "facultyId"),
    dutyType = JsonRowUtils.firstString(this, "duty_type", "dutyType"),
    status = JsonRowUtils.firstString(this, "status")
)

/** sm_questionPaper/questionPaper/view. Field names inferred from the add payload's
 * own vocabulary. */
fun JsonObject.toQuestionPaper(): QuestionPaper = QuestionPaper(
    id = JsonRowUtils.firstString(this, "id"),
    scheduleId = JsonRowUtils.firstString(this, "schedule_id", "scheduleId"),
    courseId = JsonRowUtils.firstString(this, "course_id", "courseId"),
    setBy = JsonRowUtils.firstString(this, "set_by", "setBy"),
    content = JsonRowUtils.firstString(this, "content"),
    status = JsonRowUtils.firstString(this, "status")
)

/** sm_questionPaper/questionPaperSubmission/view. */
fun JsonObject.toQuestionPaperSubmission(): QuestionPaperSubmission = QuestionPaperSubmission(
    id = JsonRowUtils.firstString(this, "id"),
    questionPaperId = JsonRowUtils.firstString(this, "question_paper_id", "questionPaperId"),
    status = JsonRowUtils.firstString(this, "status"),
    submittedAt = JsonRowUtils.firstString(this, "submitted_at", "submittedAt")
)

/** sm_questionPaper/questionBank/view. */
fun JsonObject.toQuestionBankEntry(): QuestionBankEntry = QuestionBankEntry(
    id = JsonRowUtils.firstString(this, "id"),
    courseId = JsonRowUtils.firstString(this, "course_id", "courseId"),
    questionPaperId = JsonRowUtils.firstString(this, "question_paper_id", "questionPaperId"),
    content = JsonRowUtils.firstString(this, "content")
)

/** sm_externalExam/externalExaminer/view. */
fun JsonObject.toExternalExaminer(): ExternalExaminer = ExternalExaminer(
    id = JsonRowUtils.firstString(this, "id", "examiner_id", "examinerId"),
    examinerName = JsonRowUtils.firstString(this, "examiner_name", "examinerName"),
    contactEmail = JsonRowUtils.firstString(this, "contact_email", "contactEmail", "email"),
    contactPhone = JsonRowUtils.firstString(this, "contact_phone", "contactPhone", "phone"),
    specialization = JsonRowUtils.firstString(this, "specialization")
)

/** sm_externalExam/externalPaperSetting/view. */
fun JsonObject.toExternalPaperSetting(): ExternalPaperSetting = ExternalPaperSetting(
    id = JsonRowUtils.firstString(this, "id"),
    examinerId = JsonRowUtils.firstString(this, "examiner_id", "examinerId"),
    courseId = JsonRowUtils.firstString(this, "course_id", "courseId"),
    scheduleId = JsonRowUtils.firstString(this, "schedule_id", "scheduleId")
)

/** sm_externalExam/externalEvaluation/view. */
fun JsonObject.toExternalEvaluation(): ExternalEvaluation = ExternalEvaluation(
    id = JsonRowUtils.firstString(this, "id"),
    examinerId = JsonRowUtils.firstString(this, "examiner_id", "examinerId"),
    courseId = JsonRowUtils.firstString(this, "course_id", "courseId"),
    scheduleId = JsonRowUtils.firstString(this, "schedule_id", "scheduleId"),
    scriptBundleIds = JsonRowUtils.firstStringArray(
        this, "script_bundle_ids", "scriptBundleIds"
    ),
    status = JsonRowUtils.firstString(this, "status")
)

/** sm_internalExam/internalExam/view. */
fun JsonObject.toInternalExam(): InternalExam = InternalExam(
    id = JsonRowUtils.firstString(this, "id"),
    courseOfferingId = JsonRowUtils.firstString(
        this, "course_offering_id", "courseOfferingId"
    ),
    examName = JsonRowUtils.firstString(this, "exam_name", "examName"),
    examTypeId = JsonRowUtils.firstString(this, "exam_type_id", "examTypeId"),
    examDate = JsonRowUtils.firstString(this, "exam_date", "examDate"),
    totalMaxMarks = JsonRowUtils.firstString(this, "total_max_marks", "totalMaxMarks"),
    status = JsonRowUtils.firstString(this, "status")
)

/** sm_internalExam/internalMarksEntry/view and sm_marks/marksEntry/view - shared
 * shape, see MarksRecord's doc comment for the field-naming caveat. */
fun JsonObject.toMarksRecord(): MarksRecord = MarksRecord(
    id = JsonRowUtils.firstString(this, "id"),
    scheduleId = JsonRowUtils.firstString(this, "schedule_id", "scheduleId"),
    courseId = JsonRowUtils.firstString(this, "course_id", "courseId"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studId", "student_id", "studentId"),
    marks = JsonRowUtils.firstString(this, "marks"),
    status = JsonRowUtils.firstString(this, "status")
)

/** sm_marks/grade/view. */
fun JsonObject.toGrade(): Grade = Grade(
    id = JsonRowUtils.firstString(this, "id"),
    scheduleId = JsonRowUtils.firstString(this, "schedule_id", "scheduleId"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studId", "student_id", "studentId"),
    courseId = JsonRowUtils.firstString(this, "course_id", "courseId"),
    grade = JsonRowUtils.firstString(this, "grade"),
    status = JsonRowUtils.firstString(this, "status")
)

/** sm_evaluation/evaluationBundle/view. */
fun JsonObject.toEvaluationBundle(): EvaluationBundle = EvaluationBundle(
    id = JsonRowUtils.firstString(this, "id"),
    scheduleId = JsonRowUtils.firstString(this, "schedule_id", "scheduleId"),
    courseId = JsonRowUtils.firstString(this, "course_id", "courseId"),
    evaluatorId = JsonRowUtils.firstString(this, "evaluator_id", "evaluatorId"),
    studentIds = JsonRowUtils.firstStringArray(this, "student_ids", "studentIds"),
    status = JsonRowUtils.firstString(this, "status")
)

/** sm_resultApproval/resultApproval/view. Built from the contract's documented `add`
 * response text ("Results approved at level 'ADMIN'. Chain complete: False"). */
fun JsonObject.toResultApprovalStatus(): ResultApprovalStatus = ResultApprovalStatus(
    scheduleId = JsonRowUtils.firstString(this, "schedule_id", "scheduleId"),
    level = JsonRowUtils.firstString(this, "level"),
    chainComplete = JsonRowUtils.firstString(
        this, "chain_complete", "chainComplete"
    )?.toBooleanFlag(),
    remarks = JsonRowUtils.firstString(this, "remarks")
)
