package com.xsc.oneapp.feature.login.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.login.ui.effect.VerifyOtpEffect
import com.xsc.oneapp.feature.login.ui.event.VerifyOtpEvent
import com.xsc.oneapp.feature.login.ui.viewmodel.VerifyOtpViewModel
import com.xsc.sdk.commonui.auth.AuthScreenLayout
import com.xsc.sdk.commonui.auth.BannerTone
import com.xsc.sdk.commonui.auth.InlineBanner
import com.xsc.sdk.commonui.button.PrimaryButton
import com.xsc.sdk.commonui.textfield.PremiumTextField
import kotlinx.coroutines.flow.collectLatest

/**
 * Restyled only. The effect collection, the `OtpChanged`/`Submit` dispatches and the
 * ViewModel contract are unchanged.
 *
 * The keyboard stays `Number` as before. The field actually carries the reset token, so
 * widening the accepted input would be a behaviour change rather than a visual one.
 */
@Composable
fun VerifyOtpScreen(
    onNavigateToResetPassword: (resetToken: String) -> Unit,
    viewModel: VerifyOtpViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is VerifyOtpEffect.NavigateToResetPassword ->
                    onNavigateToResetPassword(effect.resetToken)
            }
        }
    }

    AuthScreenLayout(
        icon = Icons.Rounded.Sms,
        title = "Enter verification code",
        subtitle = "Enter the code we just sent you to continue resetting your password."
    ) {
        PremiumTextField(
            text = state.otpCode,
            onTextChange = { viewModel.onEvent(VerifyOtpEvent.OtpChanged(it)) },
            placeholder = "Verification code",
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
            error = null
        )

        PrimaryButton(
            text = "Verify code",
            onClick = { viewModel.onEvent(VerifyOtpEvent.Submit) },
            isLoading = state.isLoading
        )

        InlineBanner(message = state.errorMessage, tone = BannerTone.Error)
    }
}
