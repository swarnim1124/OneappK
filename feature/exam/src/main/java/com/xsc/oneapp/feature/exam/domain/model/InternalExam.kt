package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §6.1 - a Continuous Internal Evaluation (CIE) component set for a
 * course offering. */
data class InternalExam(
    val id: String?,
    val courseOfferingId: String?,
    val examName: String?,
    val examTypeId: String?,
    val examDate: String?,
    val totalMaxMarks: String?,
    val status: String?
)
