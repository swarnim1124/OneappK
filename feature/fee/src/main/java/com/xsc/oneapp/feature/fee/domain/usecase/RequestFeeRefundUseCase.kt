package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import javax.inject.Inject

/**
 * Submits a refund request (`feeRefund:add`) for a payment already made. Distinct
 * from [ConfirmOnlinePaymentUseCase]: nothing has happened to the student's money
 * yet when this is called, so a backend rejection here is a real failure and should
 * propagate as one rather than being swallowed.
 */
class RequestFeeRefundUseCase @Inject constructor(
    private val repository: FeeRepository
) {
    suspend operator fun invoke(
        paymentId: String?,
        amount: Double,
        reason: String,
        feeType: String?
    ) = repository.requestRefund(paymentId, amount, reason, feeType)
}
