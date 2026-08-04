package com.xsc.oneapp.feature.profile.domain.usecase

import com.xsc.oneapp.feature.profile.domain.model.*
import com.xsc.oneapp.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class GetPersonalDetailUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: String? = null): PersonalDetail {
        return repository.getPersonalDetail(userId)
    }
}

class UpdatePersonalDetailUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: String? = null, fieldsToUpdate: Map<String, Any>) {
        repository.updatePersonalDetail(userId, fieldsToUpdate)
    }
}

class AddPersonalDetailUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(payload: Map<String, Any>): String {
        return repository.addPersonalDetail(payload)
    }
}
