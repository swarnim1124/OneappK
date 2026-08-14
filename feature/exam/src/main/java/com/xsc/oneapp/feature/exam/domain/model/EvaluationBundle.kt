package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §6.3 - an answer-script valuation bundle assigned to an evaluator. */
data class EvaluationBundle(
    val id: String?,
    val scheduleId: String?,
    val courseId: String?,
    val evaluatorId: String?,
    val studentIds: List<String>,
    val status: String?
)
