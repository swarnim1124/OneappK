package com.xsc.oneapp.feature.profile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.feature.profile.domain.usecase.GetUserPreferenceUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.UpdateUserPreferenceUseCase
import com.xsc.oneapp.feature.profile.ui.state.UserPreferenceEffect
import com.xsc.oneapp.feature.profile.ui.state.UserPreferenceEvent
import com.xsc.oneapp.feature.profile.ui.state.UserPreferenceState
import com.xsc.sdk.network.APIError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserPreferenceViewModel @Inject constructor(
    private val getUserPreferenceUseCase: GetUserPreferenceUseCase,
    private val updateUserPreferenceUseCase: UpdateUserPreferenceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<UserPreferenceState>(UserPreferenceState.Loading)
    val state: StateFlow<UserPreferenceState> = _state

    private val _effect = MutableSharedFlow<UserPreferenceEffect>()
    val effect: SharedFlow<UserPreferenceEffect> = _effect

    fun onEvent(event: UserPreferenceEvent) {
        when (event) {
            is UserPreferenceEvent.LoadUserPreference -> loadUserPreference()
            is UserPreferenceEvent.SaveUserPreference -> saveUserPreference(event.fieldsToUpdate)
        }
    }

    private fun loadUserPreference() {
        viewModelScope.launch {
            _state.value = UserPreferenceState.Loading
            try {
                val detail = getUserPreferenceUseCase()
                _state.value = UserPreferenceState.Success(detail)
            } catch (e: APIError.BusinessError) {
                _state.value = UserPreferenceState.BusinessError(e.message ?: "Business error occurred")
            } catch (e: APIError.NetworkError) {
                _state.value = UserPreferenceState.NetworkError(e.message ?: "Network error")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = UserPreferenceState.UnexpectedError("Unexpected error")
            }
        }
    }

    private fun saveUserPreference(fieldsToUpdate: Map<String, Any>) {
        viewModelScope.launch {
            _state.value = UserPreferenceState.Loading
            try {
                updateUserPreferenceUseCase(fieldsToUpdate = fieldsToUpdate)
                _effect.emit(UserPreferenceEffect.ShowToast("Preferences updated successfully"))
                loadUserPreference()
            } catch (e: APIError.BusinessError) {
                _state.value = UserPreferenceState.BusinessError(e.message ?: "Failed to update preferences")
            } catch (e: APIError.NetworkError) {
                _state.value = UserPreferenceState.NetworkError(e.message ?: "Network error")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = UserPreferenceState.UnexpectedError("Unexpected error")
            }
        }
    }
}
