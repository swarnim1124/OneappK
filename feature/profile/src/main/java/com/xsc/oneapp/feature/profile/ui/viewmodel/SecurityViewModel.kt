package com.xsc.oneapp.feature.profile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.feature.profile.domain.repository.SecurityRepository
import com.xsc.oneapp.feature.profile.ui.event.SecurityEvent
import com.xsc.oneapp.feature.profile.ui.state.SecurityState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val repository: SecurityRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SecurityState())
    val state: StateFlow<SecurityState> = _state.asStateFlow()

    init {
        onEvent(SecurityEvent.LoadMfaStatus)
    }

    fun onEvent(event: SecurityEvent) {
        when (event) {
            is SecurityEvent.LoadMfaStatus -> loadMfaStatus()
            is SecurityEvent.InitiateEnrollment -> initiateEnrollment()
            is SecurityEvent.OtpChanged -> {
                _state.update { it.copy(otpInput = event.otp) }
            }
            is SecurityEvent.FinalizeEnrollment -> finalizeEnrollment()
            is SecurityEvent.CloseEnrollmentModal -> {
                _state.update { it.copy(showEnrollmentModal = false, enrollmentData = null, otpInput = "") }
            }
            is SecurityEvent.RequestDisableMfa -> {
                _state.update { it.copy(showDisableConfirmation = true, methodToDisable = event.method) }
            }
            is SecurityEvent.ConfirmDisableMfa -> confirmDisableMfa()
            is SecurityEvent.CancelDisableMfa -> {
                _state.update { it.copy(showDisableConfirmation = false, methodToDisable = null) }
            }
            is SecurityEvent.RegenerateBackupCodes -> regenerateBackupCodes(event.methodId)
            is SecurityEvent.CloseBackupCodesModal -> {
                _state.update { it.copy(showBackupCodesModal = false, backupCodesToDisplay = emptyList()) }
            }
            is SecurityEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun loadMfaStatus() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val methods = repository.getMfaMethods()
                _state.update { it.copy(isLoading = false, mfaMethods = methods) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun initiateEnrollment() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val enrollment = repository.initiateMfaEnrollment()
                _state.update { it.copy(isLoading = false, enrollmentData = enrollment, showEnrollmentModal = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun finalizeEnrollment() {
        val challengeId = _state.value.enrollmentData?.enrollmentChallengeId ?: return
        val otp = _state.value.otpInput
        if (otp.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val success = repository.finalizeMfaEnrollment(challengeId, otp)
                if (success) {
                    _state.update { it.copy(showEnrollmentModal = false, enrollmentData = null, otpInput = "") }
                    loadMfaStatus()
                } else {
                    _state.update { it.copy(isLoading = false, error = "Verification failed") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun confirmDisableMfa() {
        val methodId = _state.value.methodToDisable?.id ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val success = repository.disableMfa(methodId)
                if (success) {
                    _state.update { it.copy(showDisableConfirmation = false, methodToDisable = null) }
                    loadMfaStatus()
                } else {
                    _state.update { it.copy(isLoading = false, error = "Failed to disable MFA") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun regenerateBackupCodes(methodId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val result = repository.regenerateBackupCodes(methodId)
                _state.update {
                    it.copy(
                        isLoading = false,
                        showBackupCodesModal = true,
                        backupCodesToDisplay = result.backupCodes
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
