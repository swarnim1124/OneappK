package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §5.1 - a draft/submitted/approved question paper. `content` is
 * plain text - no file-upload SDK exists anywhere in this app (confirmed earlier
 * this session), so a question paper's body is authored as text, not attached as a
 * document. */
data class QuestionPaper(
    val id: String?,
    val scheduleId: String?,
    val courseId: String?,
    val setBy: String?,
    val content: String?,
    val status: String?
)
