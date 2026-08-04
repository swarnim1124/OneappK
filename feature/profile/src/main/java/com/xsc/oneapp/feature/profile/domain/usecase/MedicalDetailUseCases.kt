package com.xsc.oneapp.feature.profile.domain.usecase

import com.xsc.oneapp.feature.profile.domain.model.MedicalDetail
import com.xsc.oneapp.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class GetMedicalDetailUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: String? = null): MedicalDetail? {
        return repository.getMedicalDetail(userId)
    }
}

class AddMedicalDetailUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(payload: Map<String, Any>) {
        repository.addMedicalDetail(payload)
    }
}

class UpdateMedicalDetailUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: String? = null, fieldsToUpdate: Map<String, Any>) {
        repository.updateMedicalDetail(userId, fieldsToUpdate)
    }
}

class DeleteMedicalDetailUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: String? = null, reason: String) {
        repository.deleteMedicalDetail(userId, reason)
    }
}
