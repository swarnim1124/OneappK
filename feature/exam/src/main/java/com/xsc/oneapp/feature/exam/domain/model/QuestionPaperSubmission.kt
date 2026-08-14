package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §5.1 - a question paper submitted for review, ahead of
 * [QuestionPaperApproval]'s (folded into [QuestionPaper.status]) print-lock. */
data class QuestionPaperSubmission(
    val id: String?,
    val questionPaperId: String?,
    val status: String?,
    val submittedAt: String?
)
