package com.xsc.oneapp.feature.fee.domain.model

/**
 * The gateway order returned by `feePayment:add` when `method` is `"ONLINE"`
 * (m_fees contract v1.3 §3.5):
 *
 * ```json
 * { "id": "order_MOCK12345678", "entity": "order", "amount": 1000000, "currency": "INR" }
 * ```
 *
 * `amount` is in the smallest currency unit (paise), which is also what Razorpay
 * Checkout expects, so it is passed straight through rather than divided and
 * re-multiplied.
 */
data class PaymentOrder(
    val orderId: String?,
    val amountInPaise: Long,
    val currency: String,
    /** The backend's own payment-row id, when it returns one. Absent in the contract's
     * documented response, so [FeeRepository.confirmOnlinePayment] falls back to the
     * gateway payment id when this is null. */
    val backendPaymentId: String?
) {
    /**
     * Whether [orderId] can be handed to Razorpay Checkout.
     *
     * The contract's own example response is `"order_MOCK12345678"` - the backend is
     * still returning a placeholder rather than a real Razorpay order. Opening
     * Checkout with a fabricated order id fails at the gateway with
     * "order_id is invalid", so a placeholder is detected here and the client opens
     * an amount-only Checkout instead (valid in test mode). Once the backend creates
     * real orders this predicate starts returning true on its own with no code change.
     */
    val isRealGatewayOrder: Boolean
        get() {
            val id = orderId ?: return false
            return id.startsWith("order_") && !id.contains("MOCK", ignoreCase = true)
        }
}
