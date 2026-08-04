package com.xsc.oneapp.feature.profile.ui.state

import com.xsc.oneapp.feature.profile.domain.model.FamilyDetail

sealed class FamilyDetailState {
    object Loading : FamilyDetailState()
    data class Success(val familyDetails: List<FamilyDetail>) : FamilyDetailState()
    object Empty : FamilyDetailState()
    data class BusinessError(val message: String) : FamilyDetailState()
    data class NetworkError(val message: String) : FamilyDetailState()
    data class UnexpectedError(val message: String) : FamilyDetailState()
}

sealed class FamilyDetailEvent {
    object LoadFamilyDetail : FamilyDetailEvent()
    data class SaveFamilyDetail(val id: Int, val fieldsToUpdate: Map<String, Any>) : FamilyDetailEvent()
    data class DeleteFamilyDetail(val id: Int, val reason: String) : FamilyDetailEvent()
}

sealed class FamilyDetailEffect {
    data class ShowToast(val message: String) : FamilyDetailEffect()
    object NavigateBack : FamilyDetailEffect()
}
