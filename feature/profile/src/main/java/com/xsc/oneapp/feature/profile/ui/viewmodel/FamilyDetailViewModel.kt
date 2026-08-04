package com.xsc.oneapp.feature.profile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.feature.profile.domain.usecase.DeleteFamilyDetailUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.GetFamilyDetailUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.GetPersonalDetailUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.UpdateFamilyDetailUseCase
import com.xsc.oneapp.feature.profile.ui.state.FamilyDetailEffect
import com.xsc.oneapp.feature.profile.ui.state.FamilyDetailEvent
import com.xsc.oneapp.feature.profile.ui.state.FamilyDetailState
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
class FamilyDetailViewModel @Inject constructor(
    private val getFamilyDetailUseCase: GetFamilyDetailUseCase,
    private val updateFamilyDetailUseCase: UpdateFamilyDetailUseCase,
    private val deleteFamilyDetailUseCase: DeleteFamilyDetailUseCase,
    /** studentId (sch_student.tb_stud.id) is NOT the signed-in user's userId
     * (sch_iam.tb_user.id) - confirmed 2026-07-31. It only comes back from the
     * backend's own join, exposed on personalDetail.view, so that's fetched first
     * rather than assuming SessionManager's userId doubles as the student id. */
    private val getPersonalDetailUseCase: GetPersonalDetailUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<FamilyDetailState>(FamilyDetailState.Loading)
    val state: StateFlow<FamilyDetailState> = _state

    private val _effect = MutableSharedFlow<FamilyDetailEffect>()
    val effect: SharedFlow<FamilyDetailEffect> = _effect

    fun onEvent(event: FamilyDetailEvent) {
        when (event) {
            is FamilyDetailEvent.LoadFamilyDetail -> loadFamilyDetail()
            is FamilyDetailEvent.SaveFamilyDetail -> saveFamilyDetail(event.id, event.fieldsToUpdate)
            is FamilyDetailEvent.DeleteFamilyDetail -> deleteFamilyDetail(event.id, event.reason)
        }
    }

    private fun loadFamilyDetail() {
        viewModelScope.launch {
            _state.value = FamilyDetailState.Loading
            try {
                // Family detail is keyed on studentId, not the signed-in userId -
                // resolve it via personalDetail.view's backend-joined value.
                val studentId = getPersonalDetailUseCase().studentId
                    ?: throw APIError.BusinessError("NO_STUDENT_ID", "No student record found for this account")
                val details = getFamilyDetailUseCase(studentId)
                if (details.isEmpty()) {
                    _state.value = FamilyDetailState.Empty
                } else {
                    _state.value = FamilyDetailState.Success(details)
                }
            } catch (e: APIError.BusinessError) {
                _state.value = FamilyDetailState.BusinessError(e.message ?: "Business error occurred")
            } catch (e: APIError.NetworkError) {
                _state.value = FamilyDetailState.NetworkError(e.message ?: "Network error")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = FamilyDetailState.UnexpectedError("Unexpected error")
            }
        }
    }

    private fun saveFamilyDetail(id: Int, fieldsToUpdate: Map<String, Any>) {
        viewModelScope.launch {
            _state.value = FamilyDetailState.Loading
            try {
                updateFamilyDetailUseCase(id, fieldsToUpdate)
                _effect.emit(FamilyDetailEffect.ShowToast("Family detail updated successfully"))
                loadFamilyDetail()
            } catch (e: APIError.BusinessError) {
                _state.value = FamilyDetailState.BusinessError(e.message ?: "Failed to update detail")
            } catch (e: APIError.NetworkError) {
                _state.value = FamilyDetailState.NetworkError(e.message ?: "Network error")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = FamilyDetailState.UnexpectedError("Unexpected error")
            }
        }
    }

    private fun deleteFamilyDetail(id: Int, reason: String) {
        viewModelScope.launch {
            _state.value = FamilyDetailState.Loading
            try {
                deleteFamilyDetailUseCase(id, reason)
                _effect.emit(FamilyDetailEffect.ShowToast("Family detail deleted"))
                loadFamilyDetail()
            } catch (e: APIError.BusinessError) {
                _state.value = FamilyDetailState.BusinessError(e.message ?: "Failed to delete detail")
            } catch (e: APIError.NetworkError) {
                _state.value = FamilyDetailState.NetworkError(e.message ?: "Network error")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = FamilyDetailState.UnexpectedError("Unexpected error")
            }
        }
    }
}
