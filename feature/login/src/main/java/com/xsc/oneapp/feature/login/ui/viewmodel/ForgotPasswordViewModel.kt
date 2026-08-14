package com.xsc.oneapp.feature.login.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.feature.login.domain.usecase.ForgotPasswordUseCase
import com.xsc.oneapp.feature.login.ui.effect.ForgotPasswordEffect
import com.xsc.oneapp.feature.login.ui.event.ForgotPasswordEvent
import com.xsc.oneapp.feature.login.ui.state.ForgotPasswordState
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
 * Owns only the forgot-password flow (email, OTP request). Kept separate from
 * LoginViewModel so this destination doesn't drag in LoginUseCase it never uses,
 * and so its state can't be confused with the login screen's state -
 * the two are independent NavBackStackEntry scopes anyway.
 */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val forgotPasswordUseCase: ForgotPasswordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    private val _effect = Channel<ForgotPasswordEffect>()
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.EmailChanged -> {
                _state.update { it.copy(forgotEmail = event.email) }
            }
            is ForgotPasswordEvent.SendOTP -> {
                sendForgotPasswordOTP()
            }
            is ForgotPasswordEvent.ClearMessages -> {
                _state.update { it.copy(errorMessage = null, successMessage = null) }
            }
        }
    }

    private fun sendForgotPasswordOTP() {
        val email = _state.value.forgotEmail

        if (email.isBlank()) {
            _state.update { it.copy(errorMessage = "Please enter your email address") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            try {
                val result = forgotPasswordUseCase(email)
                _state.update { it.copy(isLoading = false) }
                _effect.send(ForgotPasswordEffect.NavigateToVerifyOtp(result.resetToken ?: ""))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "Failed to send OTP. Please try again.") }
            }
        }
    }
}
