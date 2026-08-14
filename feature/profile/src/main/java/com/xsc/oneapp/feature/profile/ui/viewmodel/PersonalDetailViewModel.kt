package com.xsc.oneapp.feature.profile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.core.result.toAppError
import com.xsc.oneapp.feature.profile.domain.usecase.GetPersonalDetailUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.UpdatePersonalDetailUseCase
import com.xsc.oneapp.feature.profile.ui.state.PersonalDetailEffect
import com.xsc.oneapp.feature.profile.ui.state.PersonalDetailEvent
import com.xsc.oneapp.feature.profile.ui.state.PersonalDetailState
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
class PersonalDetailViewModel @Inject constructor(
    private val getPersonalDetailUseCase: GetPersonalDetailUseCase,
    private val updatePersonalDetailUseCase: UpdatePersonalDetailUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<PersonalDetailState>(PersonalDetailState.Loading)
    val state: StateFlow<PersonalDetailState> = _state

    private val _effect = MutableSharedFlow<PersonalDetailEffect>()
    val effect: SharedFlow<PersonalDetailEffect> = _effect

    fun onEvent(event: PersonalDetailEvent) {
        when (event) {
            is PersonalDetailEvent.LoadPersonalDetail -> loadPersonalDetail()
            is PersonalDetailEvent.SavePersonalDetail -> savePersonalDetail(event.fieldsToUpdate)
        }
    }

    private fun loadPersonalDetail() {
        viewModelScope.launch {
            _state.value = PersonalDetailState.Loading
            try {
                // Not passing userId so it gets it from SessionManager
                val detail = getPersonalDetailUseCase()
                _state.value = PersonalDetailState.Success(detail)
            } catch (e: APIError.BusinessError) {
                _state.value = PersonalDetailState.BusinessError(e.message ?: "Business error occurred")
            } catch (e: APIError.NetworkError) {
                val appError = e.toAppError("Could not load personal detail")
                _state.value = PersonalDetailState.NetworkError(appError.message, appError)
            } catch (e: APIError.HttpError) {
                val appError = e.toAppError("Could not load personal detail")
                _state.value = PersonalDetailState.UnexpectedError(appError.message, appError)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = PersonalDetailState.UnexpectedError("Unexpected error")
            }
        }
    }

    private fun savePersonalDetail(fieldsToUpdate: Map<String, Any>) {
        viewModelScope.launch {
            _state.value = PersonalDetailState.Loading
            try {
                updatePersonalDetailUseCase(fieldsToUpdate = fieldsToUpdate)
                _effect.emit(PersonalDetailEffect.ShowToast("Profile updated successfully"))
                loadPersonalDetail()
            } catch (e: APIError.BusinessError) {
                _state.value = PersonalDetailState.BusinessError(e.message ?: "Failed to update profile")
            } catch (e: APIError.NetworkError) {
                val appError = e.toAppError("Could not update personal detail")
                _state.value = PersonalDetailState.NetworkError(appError.message, appError)
            } catch (e: APIError.HttpError) {
                val appError = e.toAppError("Could not update personal detail")
                _state.value = PersonalDetailState.UnexpectedError(appError.message, appError)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = PersonalDetailState.UnexpectedError("Unexpected error")
            }
        }
    }
}
