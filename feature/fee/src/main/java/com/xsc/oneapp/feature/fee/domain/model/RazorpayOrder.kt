package com.xsc.oneapp.feature.fee.domain.model

/** m_fees contract §3.5: feePayment/add with `method: "ONLINE"` response. [amount] is
 * in the smallest currency unit (paise for INR), matching Razorpay's own convention -
 * the same value handed straight through to the Razorpay Checkout SDK's order_id/
 * amount fields, not re-derived from [FeeInvoice]. */
data class RazorpayOrder(
    val id: String,
    val amount: Long,
    val currency: String
)
