package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §5.2 - the external examiner registry. */
data class ExternalExaminer(
    val id: String?,
    val examinerName: String?,
    val contactEmail: String?,
    val contactPhone: String?,
    val specialization: String?
)
