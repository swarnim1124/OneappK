package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §5.1 - an archived question paper kept for reuse. */
data class QuestionBankEntry(
    val id: String?,
    val courseId: String?,
    val questionPaperId: String?,
    val content: String?
)
