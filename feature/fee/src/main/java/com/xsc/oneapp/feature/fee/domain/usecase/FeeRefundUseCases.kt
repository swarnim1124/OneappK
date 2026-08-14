package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import javax.inject.Inject

/** m_fees contract §3.6 (Fee Refund) - request is student-facing, status update is admin. */

class RequestRefundUseCase @Inject constructor(
    private val repository: FeeRepository
) {
    suspend operator fun invoke(invoiceId: String, amount: Double, reason: String) =
        repository.requestRefund(invoiceId, amount, reason)
}

class UpdateRefundStatusUseCase @Inject constructor(
    private val repository: FeeRepository
) {
    suspend operator fun invoke(refundId: String, status: String, remarks: String? = null) =
        repository.updateRefundStatus(refundId, status, remarks)
}
