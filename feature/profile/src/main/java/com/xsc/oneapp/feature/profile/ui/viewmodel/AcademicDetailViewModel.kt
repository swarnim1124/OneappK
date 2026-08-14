package com.xsc.oneapp.feature.profile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.core.result.toAppError
import com.xsc.oneapp.feature.profile.domain.usecase.GetAcademicDetailUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.UpdateAcademicIdentifiersUseCase
import com.xsc.oneapp.feature.profile.ui.state.AcademicDetailEffect
import com.xsc.oneapp.feature.profile.ui.state.AcademicDetailEvent
import com.xsc.oneapp.feature.profile.ui.state.AcademicDetailState
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
class AcademicDetailViewModel @Inject constructor(
    private val getAcademicDetailUseCase: GetAcademicDetailUseCase,
    private val updateAcademicIdentifiersUseCase: UpdateAcademicIdentifiersUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<AcademicDetailState>(AcademicDetailState.Loading)
    val state: StateFlow<AcademicDetailState> = _state

    private val _effect = MutableSharedFlow<AcademicDetailEffect>()
    val effect: SharedFlow<AcademicDetailEffect> = _effect

    fun onEvent(event: AcademicDetailEvent) {
        when (event) {
            is AcademicDetailEvent.LoadAcademicDetail -> loadAcademicDetail()
            is AcademicDetailEvent.SaveAcademicIdentifiers -> saveAcademicIdentifiers(event.idCardNumber, event.biometricId)
        }
    }

    private fun loadAcademicDetail() {
        viewModelScope.launch {
            _state.value = AcademicDetailState.Loading
            try {
                val detail = getAcademicDetailUseCase()
                _state.value = AcademicDetailState.Success(detail)
            } catch (e: APIError.BusinessError) {
                _state.value = AcademicDetailState.BusinessError(e.message ?: "Business error occurred")
            } catch (e: APIError.NetworkError) {
                val appError = e.toAppError("academic detail")
                _state.value = AcademicDetailState.NetworkError(appError.message, appError)
            } catch (e: APIError.HttpError) {
                val appError = e.toAppError("academic detail")
                _state.value = AcademicDetailState.UnexpectedError(appError.message, appError)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = AcademicDetailState.UnexpectedError("Unexpected error")
            }
        }
    }

    private fun saveAcademicIdentifiers(idCardNumber: String?, biometricId: String?) {
        viewModelScope.launch {
            _state.value = AcademicDetailState.Loading
            try {
                updateAcademicIdentifiersUseCase(idCardNumber = idCardNumber, biometricId = biometricId)
                _effect.emit(AcademicDetailEffect.ShowToast("Academic identifiers updated successfully"))
                loadAcademicDetail()
            } catch (e: APIError.BusinessError) {
                _state.value = AcademicDetailState.BusinessError(e.message ?: "Failed to update identifiers")
            } catch (e: APIError.NetworkError) {
                val appError = e.toAppError("academic identifiers")
                _state.value = AcademicDetailState.NetworkError(appError.message, appError)
            } catch (e: APIError.HttpError) {
                val appError = e.toAppError("academic identifiers")
                _state.value = AcademicDetailState.UnexpectedError(appError.message, appError)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = AcademicDetailState.UnexpectedError("Unexpected error")
            }
        }
    }
}
