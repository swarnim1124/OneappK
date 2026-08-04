package com.xsc.oneapp.feature.login.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.feature.login.domain.usecase.ResetPasswordUseCase
import com.xsc.oneapp.feature.login.ui.event.ResetPasswordEvent
import com.xsc.oneapp.feature.login.ui.state.ResetPasswordState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * No effect channel here, unlike the other screens in this flow - success and
 * failure both resolve to an inline message in state (matching
 * ForgotPasswordScreen's banner convention); there's no one-shot navigation to
 * model, since moving on from a successful reset is a plain "Back to Login" tap
 * the Composable handles directly, not something the ViewModel needs to trigger.
 */
@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    private val resetToken: String = savedStateHandle.get<String>("resetToken").orEmpty()

    private val _state = MutableStateFlow(ResetPasswordState())
    val state: StateFlow<ResetPasswordState> = _state.asStateFlow()

    fun onEvent(event: ResetPasswordEvent) {
        when (event) {
            is ResetPasswordEvent.NewPasswordChanged -> {
                _state.update { it.copy(newPassword = event.password, errorMessage = null) }
            }
            is ResetPasswordEvent.ConfirmPasswordChanged -> {
                _state.update { it.copy(confirmPassword = event.password, errorMessage = null) }
            }
            is ResetPasswordEvent.Submit -> {
                submit()
            }
        }
    }

    private fun submit() {
        val newPassword = _state.value.newPassword
        val confirmPassword = _state.value.confirmPassword

        val validationError = when {
            newPassword.isBlank() || confirmPassword.isBlank() -> "Please fill in both password fields"
            newPassword.length < 8 -> "Password must be at least 8 characters"
            newPassword != confirmPassword -> "Passwords don't match"
            else -> null
        }
        if (validationError != null) {
            _state.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = resetPasswordUseCase(resetToken, newPassword)
                _state.update { it.copy(isLoading = false, successMessage = result.message) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "Failed to reset password. Please try again.") }
            }
        }
    }
}
