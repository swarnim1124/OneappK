package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.ResultApprovalStatus
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import javax.inject.Inject

/** Admin actions for m_exam contract §7 (sm_results/sm_resultApproval): result
 * generation, the multi-level approval chain, and publication. */

class GenerateResultsUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String) = repository.generateResults(scheduleId)
}

class ApproveResultsUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(
        scheduleId: String,
        level: String,
        remarks: String? = null
    ): ResultApprovalStatus = repository.approveResults(scheduleId, level, remarks)
}

class RejectResultsUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String, level: String, remarks: String? = null) =
        repository.rejectResults(scheduleId, level, remarks)
}

class PublishResultsUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String) = repository.publishResults(scheduleId)
}

class HoldResultPublicationUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String) = repository.holdResultPublication(scheduleId)
}

class UnpublishResultsUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String) = repository.unpublishResults(scheduleId)
}
