package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import javax.inject.Inject

/** Admin write action for m_fees contract §3.3 (Fee Concession). */
class GrantConcessionUseCase @Inject constructor(
    private val repository: FeeRepository
) {
    suspend operator fun invoke(
        studentId: String,
        assignmentId: String,
        concessionType: String,
        amountOrPercent: Double,
        reason: String
    ) = repository.grantConcession(studentId, assignmentId, concessionType, amountOrPercent, reason)
}
