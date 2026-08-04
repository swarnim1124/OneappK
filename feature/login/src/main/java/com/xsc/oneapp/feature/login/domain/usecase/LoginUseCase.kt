package com.xsc.oneapp.feature.login.domain.usecase

import com.xsc.oneapp.feature.login.domain.model.LoginResult
import com.xsc.oneapp.feature.login.domain.repository.LoginRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: LoginRepository
) {
    suspend operator fun invoke(payload: Map<String, Any>): LoginResult {
        return repository.login(payload)
    }
}
