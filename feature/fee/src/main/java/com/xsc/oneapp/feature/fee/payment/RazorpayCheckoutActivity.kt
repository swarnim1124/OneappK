package com.xsc.oneapp.feature.fee.payment

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject

/**
 * Transparent host for Razorpay Checkout.
 *
 * Razorpay's SDK reports back through an Activity-implemented listener, not a
 * coroutine or a callback you can pass in, so a payment needs *some* activity to own
 * it. This one exists only to open Checkout, translate the two callbacks into an
 * activity result, and finish - which lets the Compose screen treat a card payment
 * like any other `rememberLauncherForActivityResult` round trip.
 *
 * Started with `FLAG_ACTIVITY_NO_ANIMATION`-style transparent theming from the
 * manifest so the user sees Razorpay's sheet slide up over the Fees screen rather
 * than a blank activity flashing first.
 */
class RazorpayCheckoutActivity : ComponentActivity(), PaymentResultWithDataListener {

    private var orderId: String? = null
    private var finished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Re-entering after a config change would open a second Checkout sheet over
        // the first, and the SDK reports only the last one - so open exactly once.
        if (savedInstanceState != null) return

        val keyId = intent.getStringExtra(RazorpayCheckoutContract.EXTRA_KEY_ID)
        val amount = intent.getLongExtra(RazorpayCheckoutContract.EXTRA_AMOUNT, 0L)
        orderId = intent.getStringExtra(RazorpayCheckoutContract.EXTRA_ORDER_ID)

        if (keyId.isNullOrBlank() || amount <= 0L) {
            finishWithError(0, "This payment is not configured correctly. Please contact the office.")
            return
        }

        val checkout = Checkout()
        checkout.setKeyID(keyId)

        val options = JSONObject().apply {
            put("name", intent.getStringExtra(RazorpayCheckoutContract.EXTRA_NAME) ?: "OneApp")
            put(
                "description",
                intent.getStringExtra(RazorpayCheckoutContract.EXTRA_DESCRIPTION) ?: "Fee payment"
            )
            put("currency", intent.getStringExtra(RazorpayCheckoutContract.EXTRA_CURRENCY) ?: "INR")
            put("amount", amount)
            // Razorpay validates order_id against its own records; only a real one is
            // ever put here (see RazorpayCheckoutRequest.orderId).
            orderId?.takeIf { it.isNotBlank() }?.let { put("order_id", it) }
            put("send_sms_hash", true)
            put(
                "prefill",
                JSONObject().apply {
                    intent.getStringExtra(RazorpayCheckoutContract.EXTRA_EMAIL)
                        ?.takeIf { it.isNotBlank() }?.let { put("email", it) }
                    intent.getStringExtra(RazorpayCheckoutContract.EXTRA_CONTACT)
                        ?.takeIf { it.isNotBlank() }?.let { put("contact", it) }
                }
            )
            // "Pay via UPI app": skip Checkout's card/netbanking/UPI method picker and
            // go straight to the SDK's UPI-intent sheet, which lists whichever UPI
            // apps (GPay, PhonePe, Paytm, ...) are installed on the phone and hands
            // the payment off to whichever one the user taps. See
            // RazorpayCheckoutRequest.useUpiIntentFlow / FeeViewModel.startOnlinePayment.
            if (intent.getBooleanExtra(RazorpayCheckoutContract.EXTRA_UPI_INTENT_FLOW, false)) {
                put("method", "upi")
                put("upi", JSONObject().apply { put("flow", "intent") })
            }
        }

        try {
            checkout.open(this, options)
        } catch (e: Exception) {
            // Thrown for a malformed options object or a missing/invalid key - a
            // configuration problem, not a declined card, so say so differently.
            finishWithError(0, e.message ?: "Unable to open the payment screen.")
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        val paymentId = razorpayPaymentId ?: paymentData?.paymentId
        if (paymentId.isNullOrBlank()) {
            // Success with no id is unusable downstream - the ledger has nothing to
            // reconcile against - so it is reported as an error rather than a silent
            // "paid" the backend can never match.
            finishWithError(0, "The payment completed but no reference was returned.")
            return
        }

        finishWith(
            RESULT_OK,
            Intent().apply {
                putExtra(RazorpayCheckoutContract.EXTRA_PAYMENT_ID, paymentId)
                putExtra(
                    RazorpayCheckoutContract.EXTRA_ORDER_ID,
                    paymentData?.orderId ?: orderId
                )
                putExtra(RazorpayCheckoutContract.EXTRA_SIGNATURE, paymentData?.signature)
            }
        )
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        finishWithError(code, humanReadable(response))
    }

    /**
     * Razorpay's `response` is a JSON blob whose useful part is
     * `error.description`. Showing the raw blob to a student is worse than showing
     * nothing, so it is unwrapped, with a plain fallback when the shape differs.
     */
    private fun humanReadable(response: String?): String {
        if (response.isNullOrBlank()) return "The payment could not be completed."
        return try {
            JSONObject(response)
                .optJSONObject("error")
                ?.optString("description")
                ?.takeIf { it.isNotBlank() }
                ?: "The payment could not be completed."
        } catch (e: Exception) {
            "The payment could not be completed."
        }
    }

    private fun finishWithError(code: Int, message: String) {
        finishWith(
            RazorpayCheckoutContract.RESULT_PAYMENT_ERROR,
            Intent().apply {
                putExtra(RazorpayCheckoutContract.EXTRA_ERROR_CODE, code)
                putExtra(RazorpayCheckoutContract.EXTRA_ERROR_MESSAGE, message)
            }
        )
    }

    /** Razorpay can invoke both callbacks in some cancellation paths; only the first
     * result is honoured so the caller never sees a success overwritten by an error. */
    private fun finishWith(resultCode: Int, data: Intent) {
        if (finished) return
        finished = true
        setResult(resultCode, data)
        finish()
    }
}
