package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §5.2 - an external examiner assigned to set a course's paper. */
data class ExternalPaperSetting(
    val id: String?,
    val examinerId: String?,
    val courseId: String?,
    val scheduleId: String?
)
