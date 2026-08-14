package com.xsc.oneapp.feature.profile.ui.state

import com.xsc.oneapp.core.result.AppError
import com.xsc.oneapp.feature.profile.domain.model.PersonalDetail

sealed class PersonalDetailState {
    object Loading : PersonalDetailState()
    data class Success(val personalDetail: PersonalDetail) : PersonalDetailState()
    object Empty : PersonalDetailState()
    data class BusinessError(val message: String) : PersonalDetailState()
    data class NetworkError(val message: String, val appError: AppError? = null) : PersonalDetailState()
    data class UnexpectedError(val message: String, val appError: AppError? = null) : PersonalDetailState()
}

sealed class PersonalDetailEvent {
    object LoadPersonalDetail : PersonalDetailEvent()
    data class SavePersonalDetail(val fieldsToUpdate: Map<String, Any>) : PersonalDetailEvent()
}

sealed class PersonalDetailEffect {
    data class ShowToast(val message: String) : PersonalDetailEffect()
    object NavigateBack : PersonalDetailEffect()
}
