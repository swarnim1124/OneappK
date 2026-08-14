package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §6.2 - a generated grade. Field names inferred from `grade/add`'s
 * own payload vocabulary (no confirmed `view` response example in the contract). */
data class Grade(
    val id: String?,
    val scheduleId: String?,
    val studentId: String?,
    val courseId: String?,
    val grade: String?,
    val status: String?
)
