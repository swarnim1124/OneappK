package com.xsc.oneapp.feature.exam.domain.model

/** One row per exam schedule (semester/exam cycle) - aggregate GPA/CGPA only, no
 * course-level breakdown (confirmed 2026-07-31: course marks live in
 * tb_marks_entry/tb_grade, not tb_result). */
data class ExamResult(
    val id: String?,
    val scheduleId: String?,
    val studentId: String?,
    val gpa: String?,
    val cgpa: String?,
    val resultStatus: String?,
    val createdAt: String?
)
