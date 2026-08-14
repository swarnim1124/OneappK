package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import javax.inject.Inject

/** Admin write actions for m_fees contract §3.2 (Fee Assignment). */

class AssignFeeUseCase @Inject constructor(
    private val repository: FeeRepository
) {
    suspend operator fun invoke(feeStructureId: String, termId: String, dueDate: String, studentIds: List<String>) =
        repository.assignFee(feeStructureId, termId, dueDate, studentIds)
}

class DeleteFeeAssignmentUseCase @Inject constructor(
    private val repository: FeeRepository
) {
    suspend operator fun invoke(assignmentId: String) = repository.deleteFeeAssignment(assignmentId)
}
