package com.xsc.oneapp.core.payment

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** The outcome of a Razorpay checkout attempt, translated from the SDK's own
 * callback parameters into plain types with no Razorpay dependency, so anything
 * collecting [RazorpayPaymentBridge.result] doesn't need the SDK on its classpath. */
sealed class RazorpayResult {
    data class Success(val paymentId: String) : RazorpayResult()
    data class Failure(val code: Int, val description: String?) : RazorpayResult()
}

/**
 * Bridges Razorpay's Activity-callback checkout SDK into this app's Compose/ViewModel
 * architecture.
 *
 * Razorpay's classic Android SDK requires the *hosting Activity* to implement
 * `PaymentResultWithDataListener` directly - there is no listener-object API - so the
 * checkout result arrives in `MainActivity`, not wherever the checkout was triggered
 * from (a Composable inside `:feature:fee`, which has no Activity callback of its
 * own to receive into). `MainActivity` forwards its callback here; whichever
 * ViewModel is waiting on a payment collects [result].
 *
 * A plain object, not a Hilt singleton - matches [com.xsc.oneapp.core.observability
 * .CrashReporter]'s own "shared service wrapper" convention rather than introducing
 * a new DI pattern for one bridge.
 */
object RazorpayPaymentBridge {
    private val _result = MutableSharedFlow<RazorpayResult>(extraBufferCapacity = 1)
    val result: SharedFlow<RazorpayResult> = _result.asSharedFlow()

    /** Called from MainActivity.onPaymentSuccess. Suspends only as long as it takes
     * to hand off to the buffered flow - never blocks on a collector being present. */
    suspend fun emitSuccess(paymentId: String) {
        _result.emit(RazorpayResult.Success(paymentId))
    }

    suspend fun emitFailure(code: Int, description: String?) {
        _result.emit(RazorpayResult.Failure(code, description))
    }
}
