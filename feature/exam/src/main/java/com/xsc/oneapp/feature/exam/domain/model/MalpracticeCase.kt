package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §8.4 - a malpractice case naming the student. Read-only here: this
 * app only ever shows a student a case raised against them and its outcome. Reporting
 * an incident, convening a committee and recording a verdict are invigilator/committee
 * actions with no surface in a student app. */
data class MalpracticeCase(
    val id: String?,
    val studentId: String?,
    val scheduleId: String?,
    val description: String?,
    val reportedBy: String?,
    val committee: String?,
    val hearingDate: String?,
    val verdict: String?,
    val status: String?,
    val createdAt: String?
)
