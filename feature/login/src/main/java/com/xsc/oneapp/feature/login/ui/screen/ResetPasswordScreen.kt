package com.xsc.oneapp.feature.login.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.login.ui.event.ResetPasswordEvent
import com.xsc.oneapp.feature.login.ui.viewmodel.ResetPasswordViewModel
import com.xsc.sdk.commonui.auth.AuthScreenLayout
import com.xsc.sdk.commonui.auth.BannerTone
import com.xsc.sdk.commonui.auth.InlineBanner
import com.xsc.sdk.commonui.button.PrimaryButton
import com.xsc.sdk.commonui.textfield.PremiumTextField

/**
 * Restyled only.
 *
 * The success/form branch, the shared `isPasswordVisible` toggle across both fields, and
 * every event dispatched to [ResetPasswordViewModel] are unchanged. In particular the
 * validation still lives entirely in the ViewModel - this screen deliberately does not
 * pre-validate length or matching, so the rules stay in one place.
 */
@Composable
fun ResetPasswordScreen(
    onBackToLogin: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isPasswordVisible by remember { mutableStateOf(false) }

    val isComplete = !state.successMessage.isNullOrBlank()

    AuthScreenLayout(
        icon = if (isComplete) Icons.Rounded.LockReset else Icons.Default.Lock,
        title = if (isComplete) "Password updated" else "Set a new password",
        subtitle = if (isComplete) {
            "You can now sign in with your new password."
        } else {
            "Choose a new password for your account."
        }
    ) {
        if (isComplete) {
            InlineBanner(message = state.successMessage, tone = BannerTone.Success)

            PrimaryButton(
                text = "Back to sign in",
                onClick = onBackToLogin
            )
        } else {
            PremiumTextField(
                text = state.newPassword,
                onTextChange = { viewModel.onEvent(ResetPasswordEvent.NewPasswordChanged(it)) },
                placeholder = "New password",
                icon = Icons.Default.Lock,
                imeAction = ImeAction.Next,
                isSecure = true,
                isPasswordVisible = isPasswordVisible,
                onPasswordVisibilityToggle = { isPasswordVisible = !isPasswordVisible },
                error = null
            )

            PremiumTextField(
                text = state.confirmPassword,
                onTextChange = { viewModel.onEvent(ResetPasswordEvent.ConfirmPasswordChanged(it)) },
                placeholder = "Confirm new password",
                icon = Icons.Default.Lock,
                imeAction = ImeAction.Done,
                isSecure = true,
                isPasswordVisible = isPasswordVisible,
                onPasswordVisibilityToggle = { isPasswordVisible = !isPasswordVisible },
                error = null
            )

            PrimaryButton(
                text = "Reset password",
                onClick = { viewModel.onEvent(ResetPasswordEvent.Submit) },
                isLoading = state.isLoading
            )

            InlineBanner(message = state.errorMessage, tone = BannerTone.Error)
        }
    }
}
