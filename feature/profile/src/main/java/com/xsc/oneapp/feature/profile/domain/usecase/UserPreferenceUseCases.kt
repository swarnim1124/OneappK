package com.xsc.oneapp.feature.profile.domain.usecase

import com.xsc.oneapp.feature.profile.domain.model.UserPreference
import com.xsc.oneapp.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class GetUserPreferenceUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: String? = null): UserPreference {
        return repository.getUserPreference(userId)
    }
}

class AddUserPreferenceUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: String? = null, payload: Map<String, Any>) {
        repository.addUserPreference(userId, payload)
    }
}

class UpdateUserPreferenceUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: String? = null, fieldsToUpdate: Map<String, Any>) {
        repository.updateUserPreference(userId, fieldsToUpdate)
    }
}

class ResetUserPreferenceUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: String? = null) {
        repository.resetUserPreference(userId)
    }
}
