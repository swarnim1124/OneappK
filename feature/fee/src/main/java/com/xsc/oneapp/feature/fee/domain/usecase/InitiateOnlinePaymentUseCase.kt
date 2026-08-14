package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.model.RazorpayOrder
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import javax.inject.Inject

/** m_fees contract §3.5 - creates the Razorpay order the checkout SDK opens against. */
class InitiateOnlinePaymentUseCase @Inject constructor(
    private val repository: FeeRepository
) {
    suspend operator fun invoke(invoiceId: String, amount: Double, paidBy: String): RazorpayOrder =
        repository.initiateOnlinePayment(invoiceId, amount, paidBy)
}
