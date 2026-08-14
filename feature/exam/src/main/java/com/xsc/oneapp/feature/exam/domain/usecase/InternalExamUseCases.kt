package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.InternalExam
import com.xsc.oneapp.feature.exam.domain.model.MarksRecord
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import javax.inject.Inject

/** Admin actions for m_exam contract §6.1 (sm_internalExam): CIE component setup,
 * marks entry, and consolidation. */

class GetInternalExamsUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(courseOfferingId: String? = null): List<InternalExam> =
        repository.getInternalExams(courseOfferingId)
}

class AddInternalExamUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(
        courseOfferingId: String,
        examName: String,
        examTypeId: String,
        examDate: String,
        totalMaxMarks: String
    ) = repository.addInternalExam(courseOfferingId, examName, examTypeId, examDate, totalMaxMarks)
}

class UpdateInternalExamUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(
        id: String,
        examName: String? = null,
        examDate: String? = null,
        totalMaxMarks: String? = null,
        status: String? = null
    ) = repository.updateInternalExam(id, examName, examDate, totalMaxMarks, status)
}

class DeleteInternalExamUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteInternalExam(id)
}

class GetInternalMarksEntriesUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(internalExamId: String? = null): List<MarksRecord> =
        repository.getInternalMarksEntries(internalExamId)
}

class SubmitInternalMarksEntryUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(internalExamId: String, records: List<Pair<String, String>>) =
        repository.submitInternalMarksEntry(internalExamId, records)
}

class ConsolidateInternalMarksUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(courseOfferingId: String) =
        repository.consolidateInternalMarks(courseOfferingId)
}
