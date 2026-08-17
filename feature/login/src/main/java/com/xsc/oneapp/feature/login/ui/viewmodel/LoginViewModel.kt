package com.xsc.oneapp.feature.login.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.feature.login.domain.usecase.LoginUseCase
import com.xsc.oneapp.feature.login.ui.effect.LoginEffect
import com.xsc.oneapp.feature.login.ui.event.LoginEvent
import com.xsc.oneapp.feature.login.ui.state.LoginState
import com.xsc.sdk.auth.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.xsc.sdk.network.recaptcha.RecaptchaManager

/**
 * Owns only the email/password sign-in flow. Forgot-password concerns live in
 * ForgotPasswordViewModel - keeping them separate means each destination gets its
 * own focused ViewModel instance instead of two screens sharing (and stepping on)
 * one class's state.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    // Single source of truth for the auth token: TokenManager is what AuthInterceptor
    // actually reads from, so login must persist here (see AuthInterceptor.kt / AuthModule.kt).
    private val tokenManager: TokenManager,
    private val recaptchaManager: RecaptchaManager
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _effect = Channel<LoginEffect>()
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _state.update { it.copy(emailInput = event.email) }
            }
            is LoginEvent.PasswordChanged -> {
                _state.update { it.copy(passwordInput = event.password) }
            }
            is LoginEvent.SubmitLogin -> {
                performLogin()
            }
            is LoginEvent.TogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            is LoginEvent.OtpChanged -> {
                _state.update { it.copy(otpInput = event.otp) }
            }
            is LoginEvent.SubmitMfa -> {
                performMfaVerification()
            }
            is LoginEvent.ToggleMfaMode -> {
                _state.update { it.copy(isBackupCodeMode = !it.isBackupCodeMode, otpInput = "") }
            }
            is LoginEvent.BackToLogin -> {
                _state.update { it.copy(isMfaRequired = false, challengeToken = null, otpInput = "") }
            }
        }
    }

    private fun performLogin() {
        val email = _state.value.emailInput
        val password = _state.value.passwordInput

        if (email.isBlank() || password.isBlank()) {
            viewModelScope.launch {
                _effect.send(LoginEffect.ShowToast("Please enter email and password", isError = true))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                // Fetch the reCAPTCHA token transparently (no-op until a site key is configured
                // in RecaptchaManager - see sdk/XscNetworkSDK/.../recaptcha/RecaptchaManager.kt).
                val token = recaptchaManager.execute("login")
                if (token.isNotBlank()) {
                    _effect.send(LoginEffect.ShowToast("CAPTCHA verified successfully!", isError = false))
                }

                // AAA_API_CONTRACT.md v2.0 section 3.1: session:add expects "captchaToken".
                // Only send it when we actually have one - the backend only requires it
                // when XSC_CAPTCHA_ENABLED is on, and an empty string would fail that check.
                val payload = mutableMapOf<String, Any>(
                    "username" to email,
                    "password" to password
                )
                if (token.isNotBlank()) {
                    payload["captchaToken"] = token
                }

                val result = loginUseCase(payload)

                if (result.captchaRequired) {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(LoginEffect.ShowToast("reCAPTCHA verification required", isError = true))
                } else if (result.mfaRequired && !result.challengeToken.isNullOrEmpty()) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isMfaRequired = true,
                            challengeToken = result.challengeToken
                        )
                    }
                } else if (result.mfaRequired && result.challengeToken.isNullOrEmpty()) {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(LoginEffect.ShowToast("MFA challenge token missing from server", isError = true))
                } else if (!result.token.isNullOrEmpty()) {
                    tokenManager.saveTokens(result.token, result.refreshToken ?: "")
                    result.institutionId?.let { tokenManager.saveInstitutionId(it) }
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(LoginEffect.NavigateToDashboard)
                } else {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(LoginEffect.ShowToast("Session token missing from server", isError = true))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _effect.send(LoginEffect.ShowToast(userFacingMessage(e, "Login failed. Please check your credentials and try again."), isError = true))
            }
        }
    }

    private fun performMfaVerification() {
        val challengeToken = _state.value.challengeToken ?: return
        val otp = _state.value.otpInput

        if (otp.isBlank()) {
            viewModelScope.launch {
                _effect.send(LoginEffect.ShowToast("Please enter the verification code", isError = true))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val payload = mapOf(
                    "challengeToken" to challengeToken,
                    "response" to otp
                )
                val result = loginUseCase(payload)

                if (!result.token.isNullOrEmpty()) {
                    tokenManager.saveTokens(result.token, result.refreshToken ?: "")
                    result.institutionId?.let { tokenManager.saveInstitutionId(it) }
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(LoginEffect.NavigateToDashboard)
                } else {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(LoginEffect.ShowToast("Verification successful but session token missing", isError = true))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _effect.send(LoginEffect.ShowToast(userFacingMessage(e, "Verification failed. Please try again."), isError = true))
            }
        }
    }

    /**
     * Never forward raw exception/server text straight into UI copy - it can contain
     * backend implementation details, stack-trace fragments, or untranslated strings.
     * Map to a fixed, user-facing fallback instead.
     */
    private fun userFacingMessage(e: Exception, fallback: String): String {
        return if (e is com.xsc.sdk.network.APIError.BusinessError) {
            e.errorMessage
        } else {
            fallback
        }
    }
}
