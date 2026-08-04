package com.xsc.oneapp.feature.profile.domain.usecase

import com.xsc.oneapp.feature.profile.domain.model.AcademicDetail
import com.xsc.oneapp.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class GetAcademicDetailUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: String? = null, enrollmentNumber: String? = null, employeeCode: String? = null): AcademicDetail {
        return repository.getAcademicDetail(userId, enrollmentNumber, employeeCode)
    }
}

class AddAcademicIdentifiersUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: String? = null, idCardNumber: String? = null, biometricId: String? = null) {
        repository.addAcademicIdentifiers(userId, idCardNumber, biometricId)
    }
}

class UpdateAcademicIdentifiersUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: String? = null, idCardNumber: String? = null, biometricId: String? = null) {
        repository.updateAcademicIdentifiers(userId, idCardNumber, biometricId)
    }
}

class DeleteAcademicIdentifierUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: String? = null, identifierType: String, reason: String) {
        repository.deleteAcademicIdentifier(userId, identifierType, reason)
    }
}
