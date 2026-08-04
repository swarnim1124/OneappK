package com.xsc.oneapp.feature.login.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.feature.login.domain.usecase.VerifyOTPUseCase
import com.xsc.oneapp.feature.login.ui.effect.VerifyOtpEffect
import com.xsc.oneapp.feature.login.ui.event.VerifyOtpEvent
import com.xsc.oneapp.feature.login.ui.state.VerifyOtpState
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
 * m_AAA_sm_auth.py's passwordReset/view step verifies the resetToken itself, not a
 * separate email+OTP pair - the field on this screen IS the token, carried in as a
 * nav argument and read back out via SavedStateHandle rather than threaded through
 * as a Composable parameter.
 */
@HiltViewModel
class VerifyOtpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val verifyOTPUseCase: VerifyOTPUseCase
) : ViewModel() {

    private val resetToken: String = savedStateHandle.get<String>("resetToken").orEmpty()

    private val _state = MutableStateFlow(VerifyOtpState(otpCode = resetToken))
    val state: StateFlow<VerifyOtpState> = _state.asStateFlow()

    private val _effect = Channel<VerifyOtpEffect>()
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: VerifyOtpEvent) {
        when (event) {
            is VerifyOtpEvent.OtpChanged -> {
                _state.update { it.copy(otpCode = event.code, errorMessage = null) }
            }
            is VerifyOtpEvent.Submit -> {
                submit()
            }
        }
    }

    private fun submit() {
        val token = _state.value.otpCode
        if (token.isBlank()) {
            _state.update { it.copy(errorMessage = "Please enter the verification code") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                verifyOTPUseCase(token)
                _state.update { it.copy(isLoading = false) }
                _effect.send(VerifyOtpEffect.NavigateToResetPassword(token))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "Invalid or expired code. Please try again.") }
            }
        }
    }
}
