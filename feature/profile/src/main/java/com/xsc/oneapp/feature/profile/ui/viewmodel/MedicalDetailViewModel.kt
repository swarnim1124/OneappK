package com.xsc.oneapp.feature.profile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.feature.profile.domain.usecase.GetMedicalDetailUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.UpdateMedicalDetailUseCase
import com.xsc.oneapp.feature.profile.ui.state.MedicalDetailEffect
import com.xsc.oneapp.feature.profile.ui.state.MedicalDetailEvent
import com.xsc.oneapp.feature.profile.ui.state.MedicalDetailState
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
class MedicalDetailViewModel @Inject constructor(
    private val getMedicalDetailUseCase: GetMedicalDetailUseCase,
    private val updateMedicalDetailUseCase: UpdateMedicalDetailUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<MedicalDetailState>(MedicalDetailState.Loading)
    val state: StateFlow<MedicalDetailState> = _state

    private val _effect = MutableSharedFlow<MedicalDetailEffect>()
    val effect: SharedFlow<MedicalDetailEffect> = _effect

    fun onEvent(event: MedicalDetailEvent) {
        when (event) {
            is MedicalDetailEvent.LoadMedicalDetail -> loadMedicalDetail()
            is MedicalDetailEvent.SaveMedicalDetail -> saveMedicalDetail(event.fieldsToUpdate)
        }
    }

    private fun loadMedicalDetail() {
        viewModelScope.launch {
            _state.value = MedicalDetailState.Loading
            try {
                val detail = getMedicalDetailUseCase()
                if (detail == null) {
                    _state.value = MedicalDetailState.Empty
                } else {
                    _state.value = MedicalDetailState.Success(detail)
                }
            } catch (e: APIError.BusinessError) {
                _state.value = MedicalDetailState.BusinessError(e.message ?: "Business error occurred")
            } catch (e: APIError.NetworkError) {
                _state.value = MedicalDetailState.NetworkError(e.message ?: "Network error")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = MedicalDetailState.UnexpectedError("Unexpected error")
            }
        }
    }

    private fun saveMedicalDetail(fieldsToUpdate: Map<String, Any>) {
        viewModelScope.launch {
            _state.value = MedicalDetailState.Loading
            try {
                updateMedicalDetailUseCase(fieldsToUpdate = fieldsToUpdate)
                _effect.emit(MedicalDetailEffect.ShowToast("Medical detail updated successfully"))
                loadMedicalDetail()
            } catch (e: APIError.BusinessError) {
                _state.value = MedicalDetailState.BusinessError(e.message ?: "Failed to update detail")
            } catch (e: APIError.NetworkError) {
                _state.value = MedicalDetailState.NetworkError(e.message ?: "Network error")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = MedicalDetailState.UnexpectedError("Unexpected error")
            }
        }
    }
}
