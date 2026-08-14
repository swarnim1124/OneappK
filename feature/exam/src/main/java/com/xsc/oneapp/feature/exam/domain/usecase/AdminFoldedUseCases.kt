package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import javax.inject.Inject

/** Admin actions folded onto the existing student-facing Revaluation, Re-Exams and
 * Malpractice screens rather than given a dedicated screen of their own - see the
 * approved plan's "admin actions folded into existing screens" section. */

class ProcessRevaluationUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(revaluationRequestId: String, evaluatorId: String) =
        repository.processRevaluation(revaluationRequestId, evaluatorId)
}

class CreateSupplementaryExamUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(name: String, fromDate: String, toDate: String, courseIds: List<String>) =
        repository.createSupplementaryExam(name, fromDate, toDate, courseIds)
}

class CreateReappearExamUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(
        name: String,
        academicTermId: String,
        examStartDate: String,
        examEndDate: String,
        registrationStartDate: String,
        registrationEndDate: String,
        courseIds: List<String>
    ) = repository.createReappearExam(
        name, academicTermId, examStartDate, examEndDate,
        registrationStartDate, registrationEndDate, courseIds
    )
}

class SetReappearEligibilityUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(
        studentId: String,
        courseId: String,
        reappearExamId: String,
        isEligible: Boolean,
        reason: String? = null
    ) = repository.setReappearEligibility(studentId, courseId, reappearExamId, isEligible, reason)
}

class ReportMalpracticeCaseUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(studentId: String, scheduleId: String, description: String) =
        repository.reportMalpracticeCase(studentId, scheduleId, description)
}

class RecordMalpracticeVerdictUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(
        caseId: String,
        committee: String? = null,
        hearingDate: String? = null,
        verdict: String
    ) = repository.recordMalpracticeVerdict(caseId, committee, hearingDate, verdict)
}

class DismissMalpracticeCaseUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(caseId: String) = repository.dismissMalpracticeCase(caseId)
}
