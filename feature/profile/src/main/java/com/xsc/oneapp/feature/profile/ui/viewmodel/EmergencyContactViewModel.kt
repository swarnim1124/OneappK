package com.xsc.oneapp.feature.profile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.core.result.toAppError
import com.xsc.oneapp.feature.profile.domain.usecase.AddEmergencyContactUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.DeleteEmergencyContactUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.GetEmergencyContactUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.UpdateEmergencyContactUseCase
import com.xsc.oneapp.feature.profile.ui.state.EmergencyContactEffect
import com.xsc.oneapp.feature.profile.ui.state.EmergencyContactEvent
import com.xsc.oneapp.feature.profile.ui.state.EmergencyContactState
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
class EmergencyContactViewModel @Inject constructor(
    private val getEmergencyContactUseCase: GetEmergencyContactUseCase,
    private val addEmergencyContactUseCase: AddEmergencyContactUseCase,
    private val updateEmergencyContactUseCase: UpdateEmergencyContactUseCase,
    private val deleteEmergencyContactUseCase: DeleteEmergencyContactUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<EmergencyContactState>(EmergencyContactState.Loading)
    val state: StateFlow<EmergencyContactState> = _state

    private val _effect = MutableSharedFlow<EmergencyContactEffect>()
    val effect: SharedFlow<EmergencyContactEffect> = _effect

    fun onEvent(event: EmergencyContactEvent) {
        when (event) {
            is EmergencyContactEvent.LoadEmergencyContacts -> loadEmergencyContacts()
            is EmergencyContactEvent.SaveEmergencyContact -> saveEmergencyContact(event.id, event.fieldsToUpdate)
            is EmergencyContactEvent.AddEmergencyContact -> addEmergencyContact(event.fields)
            is EmergencyContactEvent.DeleteEmergencyContact -> deleteEmergencyContact(event.id)
        }
    }

    /** Unlike Family, [addEmergencyContactUseCase] doesn't need userId added here -
     * ProfileRepositoryImpl.addEmergencyContact already resolves and injects it when
     * the payload doesn't supply one, same as every other emergency-contact call. */
    private fun addEmergencyContact(fields: Map<String, Any>) {
        viewModelScope.launch {
            _state.value = EmergencyContactState.Loading
            try {
                addEmergencyContactUseCase(fields)
                _effect.emit(EmergencyContactEffect.ShowToast("Emergency contact added"))
                loadEmergencyContacts()
            } catch (e: APIError.BusinessError) {
                _state.value = EmergencyContactState.BusinessError(e.message ?: "Failed to add contact")
            } catch (e: APIError.NetworkError) {
                val appError = e.toAppError("emergency contact")
                _state.value = EmergencyContactState.NetworkError(appError.message, appError)
            } catch (e: APIError.HttpError) {
                val appError = e.toAppError("emergency contact")
                _state.value = EmergencyContactState.UnexpectedError(appError.message, appError)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = EmergencyContactState.UnexpectedError("Unexpected error")
            }
        }
    }

    private fun loadEmergencyContacts() {
        viewModelScope.launch {
            _state.value = EmergencyContactState.Loading
            try {
                val contacts = getEmergencyContactUseCase()
                if (contacts.isEmpty()) {
                    _state.value = EmergencyContactState.Empty
                } else {
                    _state.value = EmergencyContactState.Success(contacts)
                }
            } catch (e: APIError.BusinessError) {
                _state.value = EmergencyContactState.BusinessError(e.message ?: "Business error occurred")
            } catch (e: APIError.NetworkError) {
                val appError = e.toAppError("emergency contact")
                _state.value = EmergencyContactState.NetworkError(appError.message, appError)
            } catch (e: APIError.HttpError) {
                val appError = e.toAppError("emergency contact")
                _state.value = EmergencyContactState.UnexpectedError(appError.message, appError)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = EmergencyContactState.UnexpectedError("Unexpected error")
            }
        }
    }

    private fun saveEmergencyContact(id: Int, fieldsToUpdate: Map<String, Any>) {
        viewModelScope.launch {
            _state.value = EmergencyContactState.Loading
            try {
                updateEmergencyContactUseCase(id, fieldsToUpdate)
                _effect.emit(EmergencyContactEffect.ShowToast("Emergency contact updated successfully"))
                loadEmergencyContacts()
            } catch (e: APIError.BusinessError) {
                _state.value = EmergencyContactState.BusinessError(e.message ?: "Failed to update contact")
            } catch (e: APIError.NetworkError) {
                val appError = e.toAppError("emergency contact")
                _state.value = EmergencyContactState.NetworkError(appError.message, appError)
            } catch (e: APIError.HttpError) {
                val appError = e.toAppError("emergency contact")
                _state.value = EmergencyContactState.UnexpectedError(appError.message, appError)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = EmergencyContactState.UnexpectedError("Unexpected error")
            }
        }
    }

    private fun deleteEmergencyContact(id: Int) {
        viewModelScope.launch {
            _state.value = EmergencyContactState.Loading
            try {
                deleteEmergencyContactUseCase(id)
                _effect.emit(EmergencyContactEffect.ShowToast("Emergency contact deleted"))
                loadEmergencyContacts()
            } catch (e: APIError.BusinessError) {
                _state.value = EmergencyContactState.BusinessError(e.message ?: "Failed to delete contact")
            } catch (e: APIError.NetworkError) {
                val appError = e.toAppError("emergency contact")
                _state.value = EmergencyContactState.NetworkError(appError.message, appError)
            } catch (e: APIError.HttpError) {
                val appError = e.toAppError("emergency contact")
                _state.value = EmergencyContactState.UnexpectedError(appError.message, appError)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = EmergencyContactState.UnexpectedError("Unexpected error")
            }
        }
    }
}
