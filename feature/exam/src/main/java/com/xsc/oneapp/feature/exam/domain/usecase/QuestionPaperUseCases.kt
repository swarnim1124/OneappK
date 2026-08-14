package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.QuestionBankEntry
import com.xsc.oneapp.feature.exam.domain.model.QuestionPaper
import com.xsc.oneapp.feature.exam.domain.model.QuestionPaperSubmission
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import javax.inject.Inject

/** Admin CRUD for m_exam contract §5.1 (sm_questionPaper): drafting, submission,
 * approval, and the question bank archive - all 4 actions. */

class GetQuestionPapersUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String? = null): List<QuestionPaper> =
        repository.getQuestionPapers(scheduleId)
}

class AddQuestionPaperUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String, courseId: String, content: String) =
        repository.addQuestionPaper(scheduleId, courseId, content)
}

class UpdateQuestionPaperUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(id: String, content: String? = null, status: String? = null) =
        repository.updateQuestionPaper(id, content, status)
}

class DeleteQuestionPaperUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteQuestionPaper(id)
}

class GetQuestionPaperSubmissionsUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(questionPaperId: String? = null): List<QuestionPaperSubmission> =
        repository.getQuestionPaperSubmissions(questionPaperId)
}

class SubmitQuestionPaperUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(questionPaperId: String) = repository.submitQuestionPaper(questionPaperId)
}

class DeleteQuestionPaperSubmissionUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteQuestionPaperSubmission(id)
}

class ApproveQuestionPaperUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(questionPaperId: String) = repository.approveQuestionPaper(questionPaperId)
}

class RejectQuestionPaperUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(questionPaperId: String, reason: String? = null) =
        repository.rejectQuestionPaper(questionPaperId, reason)
}

class GetQuestionBankUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(courseId: String? = null): List<QuestionBankEntry> =
        repository.getQuestionBank(courseId)
}

class AddQuestionBankEntryUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(courseId: String, content: String, questionPaperId: String? = null) =
        repository.addQuestionBankEntry(courseId, content, questionPaperId)
}

class UpdateQuestionBankEntryUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(id: String, content: String? = null) =
        repository.updateQuestionBankEntry(id, content)
}

class DeleteQuestionBankEntryUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteQuestionBankEntry(id)
}
