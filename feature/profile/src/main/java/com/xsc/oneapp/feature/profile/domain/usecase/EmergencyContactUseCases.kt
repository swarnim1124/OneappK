package com.xsc.oneapp.feature.profile.domain.usecase

import com.xsc.oneapp.feature.profile.domain.model.EmergencyContact
import com.xsc.oneapp.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class GetEmergencyContactUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: String? = null): List<EmergencyContact> {
        return repository.getEmergencyContact(userId)
    }
}

class AddEmergencyContactUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(payload: Map<String, Any>) {
        repository.addEmergencyContact(payload)
    }
}

class UpdateEmergencyContactUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(contactId: Int, fieldsToUpdate: Map<String, Any>) {
        repository.updateEmergencyContact(contactId, fieldsToUpdate)
    }
}

class DeleteEmergencyContactUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(contactId: Int) {
        repository.deleteEmergencyContact(contactId)
    }
}
