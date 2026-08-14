package com.xsc.oneapp.feature.profile.ui.state

import com.xsc.oneapp.core.result.AppError
import com.xsc.oneapp.feature.profile.domain.model.UserPreference

sealed class UserPreferenceState {
    object Loading : UserPreferenceState()
    data class Success(val userPreference: UserPreference) : UserPreferenceState()
    object Empty : UserPreferenceState()
    data class BusinessError(val message: String) : UserPreferenceState()
    data class NetworkError(val message: String, val appError: AppError? = null) : UserPreferenceState()
    data class UnexpectedError(val message: String, val appError: AppError? = null) : UserPreferenceState()
}

sealed class UserPreferenceEvent {
    object LoadUserPreference : UserPreferenceEvent()
    data class SaveUserPreference(val fieldsToUpdate: Map<String, Any>) : UserPreferenceEvent()
    object ResetUserPreference : UserPreferenceEvent()
}

sealed class UserPreferenceEffect {
    data class ShowToast(val message: String) : UserPreferenceEffect()
    object NavigateBack : UserPreferenceEffect()
}
