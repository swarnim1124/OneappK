package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.Grade
import com.xsc.oneapp.feature.exam.domain.model.MarksRecord
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import javax.inject.Inject

/** Admin actions for m_exam contract §6.2 (sm_marks): end-semester marks entry,
 * verification, grade generation, and the grade lock. */

class GetMarksEntriesUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String? = null, courseId: String? = null): List<MarksRecord> =
        repository.getMarksEntries(scheduleId, courseId)
}

class SubmitMarksEntryUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String, courseId: String, records: List<Pair<String, String>>) =
        repository.submitMarksEntry(scheduleId, courseId, records)
}

class VerifyMarksUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String, courseId: String) =
        repository.verifyMarks(scheduleId, courseId)
}

class GetGradesUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String? = null): List<Grade> = repository.getGrades(scheduleId)
}

class GenerateGradesUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String, courseId: String) =
        repository.generateGrades(scheduleId, courseId)
}

class LockGradesUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String, courseId: String) =
        repository.lockGrades(scheduleId, courseId)
}
