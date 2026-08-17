package com.xsc.oneapp.feature.fee.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The contract's own documented response for `feePayment:add` (ONLINE) is
 * `"order_MOCK12345678"`. Handing that to Razorpay Checkout fails the payment with
 * "order_id is invalid", so the placeholder has to be detectable.
 */
class PaymentOrderTest {

    @Test
    fun `a real razorpay order id is usable`() {
        assertTrue(PaymentOrder("order_PkL9aZ2X", 100L, "INR", null).isRealGatewayOrder)
    }

    @Test
    fun `the backend's mock order id is not`() {
        assertFalse(PaymentOrder("order_MOCK12345678", 100L, "INR", null).isRealGatewayOrder)
    }

    @Test
    fun `a missing or non-order id is not`() {
        assertFalse(PaymentOrder(null, 100L, "INR", null).isRealGatewayOrder)
        assertFalse(PaymentOrder("pay_ABC", 100L, "INR", null).isRealGatewayOrder)
    }
}
