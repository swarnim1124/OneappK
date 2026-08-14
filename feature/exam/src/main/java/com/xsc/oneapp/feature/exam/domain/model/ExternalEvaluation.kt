package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §5.2 - an answer script bundle dispatched to an external evaluator. */
data class ExternalEvaluation(
    val id: String?,
    val examinerId: String?,
    val courseId: String?,
    val scheduleId: String?,
    val scriptBundleIds: List<String>,
    val status: String?
)
