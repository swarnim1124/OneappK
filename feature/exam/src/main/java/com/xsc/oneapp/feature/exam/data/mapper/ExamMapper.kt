package com.xsc.oneapp.feature.exam.data.mapper

import com.google.gson.JsonObject
import com.xsc.oneapp.core.json.JsonRowUtils
import com.xsc.oneapp.feature.exam.domain.model.ChallengeRevaluation
import com.xsc.oneapp.feature.exam.domain.model.ExamResult
import com.xsc.oneapp.feature.exam.domain.model.ExamSchedule
import com.xsc.oneapp.feature.exam.domain.model.HallTicket
import com.xsc.oneapp.feature.exam.domain.model.RevaluationRequest

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
