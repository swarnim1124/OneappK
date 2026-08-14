package com.xsc.oneapp.feature.profile.ui.state

import com.xsc.oneapp.core.result.AppError
import com.xsc.oneapp.feature.profile.domain.model.AcademicDetail

sealed class AcademicDetailState {
    object Loading : AcademicDetailState()
    data class Success(val academicDetail: AcademicDetail) : AcademicDetailState()
    object Empty : AcademicDetailState()
    data class BusinessError(val message: String) : AcademicDetailState()
    data class NetworkError(val message: String, val appError: AppError? = null) : AcademicDetailState()
    data class UnexpectedError(val message: String, val appError: AppError? = null) : AcademicDetailState()
}

sealed class AcademicDetailEvent {
    object LoadAcademicDetail : AcademicDetailEvent()
    data class SaveAcademicIdentifiers(val idCardNumber: String?, val biometricId: String?) : AcademicDetailEvent()
}

sealed class AcademicDetailEffect {
    data class ShowToast(val message: String) : AcademicDetailEffect()
    object NavigateBack : AcademicDetailEffect()
}
