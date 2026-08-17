package com.xsc.oneapp.feature.fee.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

/**
 * Everything Razorpay Checkout needs for one payment attempt.
 *
 * [orderId] is nullable on purpose. Razorpay accepts an amount-only Checkout (valid in
 * test mode) as well as an order-backed one, and m_fees currently returns a placeholder
 * order id (`order_MOCK...` - see [com.xsc.oneapp.feature.fee.domain.model.PaymentOrder]).
 * Passing a fabricated order id to the gateway fails the whole payment with
 * "order_id is invalid", so it is dropped rather than forwarded when it isn't real.
 */
data class RazorpayCheckoutRequest(
    val keyId: String,
    val orderId: String?,
    val amountInPaise: Long,
    val currency: String,
    val merchantName: String,
    val description: String,
    val email: String?,
    val contact: String?,
    /**
     * When true, Checkout is opened straight into Razorpay's UPI-intent flow - the
     * user picks from whichever UPI apps (GPay, PhonePe, Paytm, ...) are installed on
     * the phone and pays there, instead of seeing the full card/netbanking/UPI method
     * list first. See [RazorpayCheckoutActivity] for how this maps to the SDK's
     * `method`/`upi.flow` options.
     */
    val useUpiIntentFlow: Boolean = false
)

/** Outcome of one Checkout attempt. */
sealed interface RazorpayCheckoutResult {
    /** The gateway charged the card. [paymentId] is Razorpay's `razorpay_payment_id`. */
    data class Success(
        val paymentId: String,
        val orderId: String?,
        val signature: String?
    ) : RazorpayCheckoutResult

    /** The gateway declined or errored. Nothing was charged. */
    data class Failed(val code: Int, val message: String) : RazorpayCheckoutResult

    /** The user backed out, or the activity was destroyed before returning a result. */
    data object Cancelled : RazorpayCheckoutResult
}

/**
 * Launches [RazorpayCheckoutActivity] and maps its result back.
 *
 * A dedicated activity + contract, rather than MainActivity implementing Razorpay's
 * `PaymentResultWithDataListener`: the listener is an Activity-level callback, and
 * hanging it off MainActivity would mean the app's single Compose host owning a
 * fee-module concern and routing the result back down through a shared holder. This
 * keeps the whole gateway surface inside :feature:fee, and the screen consumes it with
 * an ordinary `rememberLauncherForActivityResult`.
 */
class RazorpayCheckoutContract :
    ActivityResultContract<RazorpayCheckoutRequest, RazorpayCheckoutResult>() {

    override fun createIntent(context: Context, input: RazorpayCheckoutRequest): Intent =
        Intent(context, RazorpayCheckoutActivity::class.java).apply {
            putExtra(EXTRA_KEY_ID, input.keyId)
            putExtra(EXTRA_ORDER_ID, input.orderId)
            putExtra(EXTRA_AMOUNT, input.amountInPaise)
            putExtra(EXTRA_CURRENCY, input.currency)
            putExtra(EXTRA_NAME, input.merchantName)
            putExtra(EXTRA_DESCRIPTION, input.description)
            putExtra(EXTRA_EMAIL, input.email)
            putExtra(EXTRA_CONTACT, input.contact)
            putExtra(EXTRA_UPI_INTENT_FLOW, input.useUpiIntentFlow)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): RazorpayCheckoutResult {
        if (intent == null) return RazorpayCheckoutResult.Cancelled

        return when (resultCode) {
            Activity.RESULT_OK -> {
                val paymentId = intent.getStringExtra(EXTRA_PAYMENT_ID)
                if (paymentId.isNullOrBlank()) {
                    RazorpayCheckoutResult.Cancelled
                } else {
                    RazorpayCheckoutResult.Success(
                        paymentId = paymentId,
                        orderId = intent.getStringExtra(EXTRA_ORDER_ID),
                        signature = intent.getStringExtra(EXTRA_SIGNATURE)
                    )
                }
            }
            RESULT_PAYMENT_ERROR -> RazorpayCheckoutResult.Failed(
                code = intent.getIntExtra(EXTRA_ERROR_CODE, 0),
                message = intent.getStringExtra(EXTRA_ERROR_MESSAGE)
                    ?: "The payment could not be completed."
            )
            else -> RazorpayCheckoutResult.Cancelled
        }
    }

    companion object {
        const val RESULT_PAYMENT_ERROR = Activity.RESULT_FIRST_USER + 1

        const val EXTRA_KEY_ID = "razorpay.keyId"
        const val EXTRA_ORDER_ID = "razorpay.orderId"
        const val EXTRA_AMOUNT = "razorpay.amount"
        const val EXTRA_CURRENCY = "razorpay.currency"
        const val EXTRA_NAME = "razorpay.name"
        const val EXTRA_DESCRIPTION = "razorpay.description"
        const val EXTRA_EMAIL = "razorpay.email"
        const val EXTRA_CONTACT = "razorpay.contact"
        const val EXTRA_UPI_INTENT_FLOW = "razorpay.upiIntentFlow"

        const val EXTRA_PAYMENT_ID = "razorpay.paymentId"
        const val EXTRA_SIGNATURE = "razorpay.signature"
        const val EXTRA_ERROR_CODE = "razorpay.errorCode"
        const val EXTRA_ERROR_MESSAGE = "razorpay.errorMessage"
    }
}
