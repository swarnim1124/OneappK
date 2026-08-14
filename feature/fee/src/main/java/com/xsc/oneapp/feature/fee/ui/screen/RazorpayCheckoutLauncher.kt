package com.xsc.oneapp.feature.fee.ui.screen

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.razorpay.Checkout
import com.xsc.oneapp.core.payment.RazorpayPaymentBridge
import com.xsc.oneapp.core.payment.RazorpayResult
import com.xsc.oneapp.feature.fee.BuildConfig
import com.xsc.oneapp.feature.fee.domain.model.RazorpayOrder
import com.xsc.sdk.commonui.record.ErrorState
import com.xsc.sdk.commonui.record.LoadingState
import com.xsc.sdk.commonui.record.RecordScaffold
import org.json.JSONObject

private sealed class LauncherState {
    data object CreatingOrder : LauncherState()
    data object AwaitingResult : LauncherState()
    data class Error(val message: String) : LauncherState()
}

/**
 * Creates the Razorpay order (via [initiateOnlinePayment]) and opens the checkout
 * SDK against it, then waits on [RazorpayPaymentBridge] for the result.
 *
 * The result doesn't arrive here directly: Razorpay's classic Android SDK requires
 * the *hosting Activity* to implement `PaymentResultWithDataListener` and delivers
 * the callback there, not to whichever Composable triggered checkout - see
 * `MainActivity` and [RazorpayPaymentBridge]'s own kdoc for the full rationale. This
 * screen just opens checkout against the current Activity (obtained via
 * [LocalContext], which is that same `MainActivity` instance for every screen in
 * this app's single-Activity NavHost) and then listens on the bridge.
 *
 * Known simplification: the bridge has no per-payment correlation id, so if this
 * screen is re-entered while a stale buffered result from a *previous* attempt
 * hasn't been drained yet, it could misread that as the new attempt's outcome. In
 * practice a payment result arrives and is consumed synchronously before the user
 * could plausibly start a second one, so this is an accepted, low-probability edge
 * case rather than a built-out correlation system.
 */
@Composable
internal fun RazorpayCheckoutLauncher(
    invoiceId: String,
    amount: Double,
    onBack: () -> Unit,
    initiateOnlinePayment: suspend (invoiceId: String, amount: Double) -> RazorpayOrder,
    onPaymentSubmitted: () -> Unit
) {
    val context = LocalContext.current
    var state by remember { mutableStateOf<LauncherState>(LauncherState.CreatingOrder) }

    LaunchedEffect(invoiceId, amount) {
        val activity = context as? Activity
        if (activity == null) {
            state = LauncherState.Error("Unable to open checkout from this screen")
            return@LaunchedEffect
        }
        try {
            val order = initiateOnlinePayment(invoiceId, amount)
            val checkout = Checkout()
            checkout.setKeyID(BuildConfig.RAZORPAY_KEY_ID)
            val options = JSONObject().apply {
                put("name", "OneApp")
                put("description", "Fee payment")
                put("order_id", order.id)
                put("currency", order.currency)
                put("amount", order.amount)
            }
            state = LauncherState.AwaitingResult
            checkout.open(activity, options)
        } catch (e: Exception) {
            state = LauncherState.Error(e.message ?: "Could not start payment")
        }
    }

    LaunchedEffect(Unit) {
        RazorpayPaymentBridge.result.collect { result ->
            when (result) {
                is RazorpayResult.Success -> onPaymentSubmitted()
                is RazorpayResult.Failure -> state = LauncherState.Error(result.description ?: "Payment was not completed")
            }
        }
    }

    RecordScaffold(title = "Payment", onBack = onBack) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when (val current = state) {
                is LauncherState.CreatingOrder, is LauncherState.AwaitingResult -> LoadingState()
                is LauncherState.Error -> ErrorState(current.message, onRetry = onBack)
            }
        }
    }
}
