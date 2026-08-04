package com.xsc.oneapp.feature.profile.ui.state

import com.xsc.oneapp.feature.profile.domain.model.EmergencyContact

sealed class EmergencyContactState {
    object Loading : EmergencyContactState()
    data class Success(val contacts: List<EmergencyContact>) : EmergencyContactState()
    object Empty : EmergencyContactState()
    data class BusinessError(val message: String) : EmergencyContactState()
    data class NetworkError(val message: String) : EmergencyContactState()
    data class UnexpectedError(val message: String) : EmergencyContactState()
}

sealed class EmergencyContactEvent {
    object LoadEmergencyContacts : EmergencyContactEvent()
    data class SaveEmergencyContact(val id: Int, val fieldsToUpdate: Map<String, Any>) : EmergencyContactEvent()
    data class DeleteEmergencyContact(val id: Int) : EmergencyContactEvent()
}

sealed class EmergencyContactEffect {
    data class ShowToast(val message: String) : EmergencyContactEffect()
    object NavigateBack : EmergencyContactEffect()
}
