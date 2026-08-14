package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §8.1 - a formal appeal against a revaluation outcome. [referenceId]
 * is the id of whatever is being appealed (a revaluation request), which the contract
 * keeps deliberately generic rather than typing it to one source. */
data class ExamAppeal(
    val id: String?,
    val studentId: String?,
    val referenceId: String?,
    val reason: String?,
    val status: String?,
    val createdAt: String?
)
