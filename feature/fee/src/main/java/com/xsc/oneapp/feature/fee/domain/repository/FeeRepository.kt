package com.xsc.oneapp.feature.fee.domain.repository

import com.xsc.oneapp.feature.fee.domain.model.FeeAssignment
import com.xsc.oneapp.feature.fee.domain.model.FeeConcession
import com.xsc.oneapp.feature.fee.domain.model.FeePayment
import com.xsc.oneapp.feature.fee.domain.model.FeePenalty
import com.xsc.oneapp.feature.fee.domain.model.FeeRefund
import com.xsc.oneapp.feature.fee.domain.model.FeeStatement
import com.xsc.oneapp.feature.fee.domain.model.FeeStructure
import com.xsc.oneapp.feature.fee.domain.model.PaymentOrder

interface FeeRepository {
    suspend fun getFeeStructures(): List<FeeStructure>
    suspend fun getMyFeeAssignments(): List<FeeAssignment>
    suspend fun getMyFeeConcessions(): List<FeeConcession>

    /** `feeInvoice:view` - a computed statement for the signed-in student, not a row
     * list. See [FeeStatement] for why this replaced `getMyFeeInvoices()`. */
    suspend fun getMyFeeStatement(): FeeStatement
    suspend fun getMyFeePayments(): List<FeePayment>
    suspend fun getMyFeeRefunds(): List<FeeRefund>
    suspend fun getFeePenalties(): List<FeePenalty>

    /**
     * `feePayment:add` with `method: "ONLINE"` - asks the backend to open a gateway
     * order. Nothing is charged here; the returned [PaymentOrder] is what Razorpay
     * Checkout is launched with.
     */
    suspend fun createOnlinePaymentOrder(
        invoiceId: String?,
        amount: Double,
        paidBy: String
    ): PaymentOrder

    /**
     * `feePayment:update` - tells the backend a gateway payment completed.
     *
     * The money has already moved by the time this is called, so a failure here means
     * "paid but not yet reconciled", never "not paid". Returns false in that case
     * rather than throwing, so the UI can say the accurate thing.
     */
    suspend fun confirmOnlinePayment(
        paymentId: String,
        succeeded: Boolean
    ): Boolean

    /**
     * `feeRefund:add` - asks the accounts office to refund a payment. Throws
     * [com.xsc.sdk.network.APIError] on rejection (nothing has moved yet at this
     * point, unlike [confirmOnlinePayment], so a failure here is a real failure and
     * should surface as one).
     */
    suspend fun requestRefund(
        paymentId: String?,
        amount: Double,
        reason: String,
        feeType: String?
    )
}
