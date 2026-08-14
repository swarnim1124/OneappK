package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.ExamAppeal
import com.xsc.oneapp.feature.exam.domain.model.PhotocopyRequest
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import javax.inject.Inject

/** The write half of sm_revaluation, plus the two read actions
 * ([GetMyPhotocopyRequestsUseCase], [GetMyAppealsUseCase]) that had no client until
 * now. Grouped in one file because they are only ever used together, by the single
 * revaluation screen. */

class SubmitRevaluationRequestUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String, courseId: String, reason: String? = null) =
        repository.submitRevaluationRequest(scheduleId, courseId, reason)
}

class GetMyPhotocopyRequestsUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(): List<PhotocopyRequest> = repository.getMyPhotocopyRequests()
}

class SubmitPhotocopyRequestUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String, courseId: String) =
        repository.submitPhotocopyRequest(scheduleId, courseId)
}

class GetMyAppealsUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(): List<ExamAppeal> = repository.getMyAppeals()
}

class SubmitAppealUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(referenceId: String, reason: String) =
        repository.submitAppeal(referenceId, reason)
}

class SubmitChallengeRevaluationUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(
        scheduleId: String,
        courseId: String,
        revaluationRequestId: String
    ) = repository.submitChallengeRevaluation(scheduleId, courseId, revaluationRequestId)
}
