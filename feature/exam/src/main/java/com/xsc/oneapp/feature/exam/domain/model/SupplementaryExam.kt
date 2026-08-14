package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §8.2 - a supplementary (backlog) examination session a student with
 * failed courses can register against. */
data class SupplementaryExam(
    val id: String?,
    val name: String?,
    val fromDate: String?,
    val toDate: String?,
    val status: String?,
    val courses: List<String>
)
