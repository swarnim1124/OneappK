package com.xsc.oneapp.feature.login.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.xsc.oneapp.feature.login.ui.event.ForgotPasswordEvent
import com.xsc.oneapp.feature.login.ui.state.ForgotPasswordState
import com.xsc.sdk.commonui.auth.AuthScreenLayout
import com.xsc.sdk.commonui.auth.BannerTone
import com.xsc.sdk.commonui.auth.InlineBanner
import com.xsc.sdk.commonui.button.PrimaryButton
import com.xsc.sdk.commonui.textfield.PremiumTextField

/**
 * Restyled only. The `ClearMessages` event on entry, the `EmailChanged`/`SendOTP`
 * dispatches and the state read are unchanged; this screen still owns no state and
 * performs no validation.
 *
 * `onBack` remains part of the signature - RootNavHost passes it - even though, as
 * before, the layout offers no explicit back control beyond the system gesture.
 */
@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordState,
    onEvent: (ForgotPasswordEvent) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) {
        onEvent(ForgotPasswordEvent.ClearMessages)
    }

    AuthScreenLayout(
        icon = Icons.Rounded.MarkEmailRead,
        title = "Forgot password?",
        subtitle = "Enter your email address and we'll send you a code to reset your password."
    ) {
        PremiumTextField(
            text = state.forgotEmail,
            onTextChange = { onEvent(ForgotPasswordEvent.EmailChanged(it)) },
            placeholder = "Email address",
            icon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
            error = null
        )

        PrimaryButton(
            text = "Send reset code",
            onClick = { onEvent(ForgotPasswordEvent.SendOTP) },
            isLoading = state.isLoading
        )

        InlineBanner(message = state.errorMessage, tone = BannerTone.Error)
        InlineBanner(message = state.successMessage, tone = BannerTone.Success)
    }
}
