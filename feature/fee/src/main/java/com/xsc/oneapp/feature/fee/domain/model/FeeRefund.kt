package com.xsc.oneapp.feature.fee.domain.model

data class FeeRefund(
    val id: String?,
    val paymentId: String?,
    val amount: String?,
    val reason: String?,
    val refundDate: String?,
    val statusId: String?,
    val approvedBy: String?,
    /** The fee category this refund belongs to (e.g. "Academic Fees", "Hostel Fees") -
     * drives the Refunds tab filter. Not in the documented `feeRefund:view` response
     * shape, so this is read defensively through every plausible key rather than
     * assumed absent - same "doesn't hurt if unused, helps if present" reasoning as
     * FeeInvoice.transactionTypeId. */
    val feeType: String? = null
)
