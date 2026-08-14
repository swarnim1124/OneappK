package com.xsc.oneapp.feature.login.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.core.result.AppError
import com.xsc.oneapp.core.result.toAppError
import com.xsc.oneapp.feature.login.domain.usecase.LoginUseCase
import com.xsc.oneapp.feature.login.ui.effect.LoginEffect
import com.xsc.oneapp.feature.login.ui.event.LoginEvent
import com.xsc.oneapp.feature.login.ui.state.LoginError
import com.xsc.oneapp.feature.login.ui.state.LoginState
import com.xsc.sdk.auth.TokenManager
import com.xsc.sdk.network.APIError
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
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _effect = Channel<LoginEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        // A fresh LoginViewModel instance is created every time RootNavHost
        // (re)navigates to Login - including the one case this flag is set for, a
        // forced session end (see TokenAuthenticator/TokenManager.expireSession).
        // Consumed immediately so it can't reappear on a later, unrelated visit.
        if (tokenManager.sessionExpired.value) {
            tokenManager.consumeSessionExpired()
            _state.update { it.copy(error = LoginError.SessionExpired) }
        }
    }

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                // Deliberately does not touch `error` - a message persists through
                // keystrokes until the next submit attempt, per spec.
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
            is LoginEvent.NavigatingAway -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun performLogin() {
        // New action started - clear whatever was left from the previous attempt,
        // whether or not this one turns out to fail client-side validation too.
        _state.update { it.copy(error = null) }

        val email = _state.value.emailInput
        val password = _state.value.passwordInput

        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = LoginError.IncompleteFields) }
            return
        }
        if (looksLikeMalformedEmail(email)) {
            _state.update { it.copy(error = LoginError.InvalidEmailFormat) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val payload = mutableMapOf<String, Any>(
                    "username" to email,
                    "password" to password
                )

                val result = loginUseCase(payload)

                if (!result.token.isNullOrEmpty()) {
                    tokenManager.saveTokens(result.token, result.refreshToken ?: "")
                    result.institutionId?.let { tokenManager.saveInstitutionId(it) }
                    result.email?.let { tokenManager.saveEmail(it) }
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(LoginEffect.NavigateToDashboard)
                } else {
                    _state.update { it.copy(isLoading = false, error = LoginError.Unknown()) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: APIError.BusinessError) {
                _state.update { it.copy(isLoading = false, error = classifyBusinessError(e)) }
            } catch (e: APIError.NetworkError) {
                @Suppress("UNCHECKED_CAST")
                val appError = e.toAppError("Could not sign in") as AppError.Network
                _state.update { it.copy(isLoading = false, error = LoginError.Network(appError)) }
            } catch (e: APIError.HttpError) {
                @Suppress("UNCHECKED_CAST")
                val appError = e.toAppError("Could not sign in") as AppError.Traced
                _state.update { it.copy(isLoading = false, error = LoginError.Traced(appError)) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = LoginError.Unknown()) }
            }
        }
    }

    /**
     * The sign-in field accepts a username as well as an email (its own placeholder
     * says "Email or Username", and the backend payload key is `username` - see
     * AAA_API_CONTRACT.md §3.1), so this only flags input that clearly *attempted*
     * to be an email and got the shape wrong - anything without an "@" is treated as
     * a username and passed straight through, same as before this change.
     */
    private fun looksLikeMalformedEmail(input: String): Boolean =
        "@" in input && !EMAIL_REGEX.matches(input)

    /**
     * No confirmed backend error-code enumeration exists for login specifically
     * (unlike modules with a documented *_API_CONTRACT.md error-code table) - this is
     * a best-effort classification by substring, falling back to the backend's own
     * [APIError.BusinessError.errorMessage] (real, specific text, not a guess) rather
     * than assuming "wrong password" for anything unrecognised.
     */
    private fun classifyBusinessError(error: APIError.BusinessError): LoginError {
        val code = error.code.uppercase()
        return when {
            "LOCK" in code -> LoginError.AccountLocked
            "CREDENTIAL" in code || "INVALID_LOGIN" in code || "INVALID_PASSWORD" in code -> LoginError.InvalidCredentials
            else -> LoginError.Unknown(AppError.Unknown(error.errorMessage.ifBlank { "Something went wrong. Try again." }))
        }
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
