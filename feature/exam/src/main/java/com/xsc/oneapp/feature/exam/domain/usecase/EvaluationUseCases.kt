package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.EvaluationBundle
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import javax.inject.Inject

/** Admin actions for m_exam contract §6.3 (sm_evaluation): answer-script bundle
 * dispatch, second valuation requests, and moderation adjustments. */

class GetEvaluationBundlesUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String? = null): List<EvaluationBundle> =
        repository.getEvaluationBundles(scheduleId)
}

class AddEvaluationBundleUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(
        scheduleId: String,
        courseId: String,
        evaluatorId: String,
        studentIds: List<String>
    ) = repository.addEvaluationBundle(scheduleId, courseId, evaluatorId, studentIds)
}

class UpdateEvaluationBundleUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(id: String, status: String? = null) =
        repository.updateEvaluationBundle(id, status)
}

class DeleteEvaluationBundleUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteEvaluationBundle(id)
}

class RequestSecondValuationUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String, courseId: String, studentId: String) =
        repository.requestSecondValuation(scheduleId, courseId, studentId)
}

class ApplyModerationUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String, courseId: String, adjustment: String) =
        repository.applyModeration(scheduleId, courseId, adjustment)
}
