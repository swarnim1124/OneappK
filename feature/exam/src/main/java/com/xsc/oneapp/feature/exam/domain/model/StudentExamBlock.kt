package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §4.3 - an examination hold placed on a student (fee arrears,
 * attendance shortage), which is why a hall ticket may be absent for a schedule the
 * student is otherwise registered for. Surfaced on the hall ticket screen so an empty
 * result carries its reason instead of reading as "not generated yet". */
data class StudentExamBlock(
    val id: String?,
    val studentId: String?,
    val scheduleId: String?,
    val reason: String?,
    val blockedBy: String?,
    val status: String?,
    val createdAt: String?
)
