package com.xsc.oneapp.feature.profile.ui.state

import com.xsc.oneapp.core.result.AppError
import com.xsc.oneapp.feature.profile.domain.model.MedicalDetail

sealed class MedicalDetailState {
    object Loading : MedicalDetailState()
    data class Success(val medicalDetail: MedicalDetail) : MedicalDetailState()
    object Empty : MedicalDetailState()
    data class BusinessError(val message: String) : MedicalDetailState()
    data class NetworkError(val message: String, val appError: AppError? = null) : MedicalDetailState()
    data class UnexpectedError(val message: String, val appError: AppError? = null) : MedicalDetailState()
}

sealed class MedicalDetailEvent {
    object LoadMedicalDetail : MedicalDetailEvent()
    data class SaveMedicalDetail(val fieldsToUpdate: Map<String, Any>) : MedicalDetailEvent()
}

sealed class MedicalDetailEffect {
    data class ShowToast(val message: String) : MedicalDetailEffect()
    object NavigateBack : MedicalDetailEffect()
}
