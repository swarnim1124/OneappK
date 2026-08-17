package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.model.PaymentOrder
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import javax.inject.Inject

/**
 * Opens a gateway order for an outstanding balance. Charges nothing on its own - the
 * returned [PaymentOrder] is what Razorpay Checkout is launched with.
 */
class CreateOnlinePaymentOrderUseCase @Inject constructor(
    private val repository: FeeRepository
) {
    suspend operator fun invoke(
        invoiceId: String?,
        amount: Double,
        paidBy: String
    ): PaymentOrder = repository.createOnlinePaymentOrder(invoiceId, amount, paidBy)
}

/**
 * Reports a completed gateway payment back to the ledger. Returns false when the
 * backend would not accept the update - the payment still happened, so callers must
 * not present that as a failed payment.
 */
class ConfirmOnlinePaymentUseCase @Inject constructor(
    private val repository: FeeRepository
) {
    suspend operator fun invoke(paymentId: String, succeeded: Boolean): Boolean =
        repository.confirmOnlinePayment(paymentId, succeeded)
}
