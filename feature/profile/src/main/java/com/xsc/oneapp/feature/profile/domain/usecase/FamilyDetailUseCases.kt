package com.xsc.oneapp.feature.profile.domain.usecase

import com.xsc.oneapp.feature.profile.domain.model.FamilyDetail
import com.xsc.oneapp.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class GetFamilyDetailUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(studentId: String): List<FamilyDetail> {
        return repository.getFamilyDetail(studentId)
    }
}

class AddFamilyDetailUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(payload: Map<String, Any>) {
        repository.addFamilyDetail(payload)
    }
}

class UpdateFamilyDetailUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(familyDetailId: Int, fieldsToUpdate: Map<String, Any>) {
        repository.updateFamilyDetail(familyDetailId, fieldsToUpdate)
    }
}

class DeleteFamilyDetailUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(familyDetailId: Int, reason: String) {
        repository.deleteFamilyDetail(familyDetailId, reason)
    }
}
