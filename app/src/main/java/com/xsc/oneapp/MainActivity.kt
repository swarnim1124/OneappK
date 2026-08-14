package com.xsc.oneapp

import android.os.Bundle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import com.xsc.oneapp.core.payment.RazorpayPaymentBridge
import com.xsc.oneapp.navigation.RootNavHost
import com.xsc.sdk.theme.LocalDarkTheme
import com.xsc.sdk.theme.LocalThemeToggle
import com.xsc.sdk.theme.OneAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Implements [PaymentResultWithDataListener] because Razorpay's classic Android
 * checkout SDK requires the *hosting Activity* to implement it directly - there is
 * no listener-object API to pass in instead. The actual checkout trigger
 * (`Checkout().open(activity, options)`) happens from a Composable inside
 * `:feature:fee`, passing this same Activity instance; the result lands here and is
 * forwarded into [RazorpayPaymentBridge] for whichever ViewModel is waiting on it -
 * see that class's kdoc for the full rationale.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity(), PaymentResultWithDataListener {

    override fun onPaymentSuccess(razorpayPaymentId: String, paymentData: PaymentData?) {
        lifecycleScope.launch { RazorpayPaymentBridge.emitSuccess(razorpayPaymentId) }
    }

    override fun onPaymentError(code: Int, description: String?, paymentData: PaymentData?) {
        lifecycleScope.launch { RazorpayPaymentBridge.emitFailure(code, description) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val isDarkMode by mainViewModel.isDarkMode.collectAsStateWithLifecycle()

            CompositionLocalProvider(
                LocalThemeToggle provides { mainViewModel.toggleTheme() },
                LocalDarkTheme provides isDarkMode,
            ) {
                OneAppTheme(darkTheme = isDarkMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        val navController = rememberNavController()
                        RootNavHost(navController = navController)
                    }
                }
            }
        }
    }
}
