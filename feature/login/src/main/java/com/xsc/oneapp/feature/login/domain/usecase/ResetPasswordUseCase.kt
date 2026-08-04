package com.xsc.oneapp.feature.login.domain.usecase

import com.xsc.oneapp.feature.login.domain.model.ResetPasswordResult
import com.xsc.oneapp.feature.login.domain.repository.LoginRepository
import javax.inject.Inject

/** m_AAA_sm_auth.py's passwordReset/update step takes only the token and the new
 * password - no email/phone (confirmed against the real source, 2026-07-31). */
class ResetPasswordUseCase @Inject constructor(
    private val repository: LoginRepository
) {
    suspend operator fun invoke(token: String, newPassword: String): ResetPasswordResult {
        return repository.resetPassword(
            mapOf("token" to token, "newPassword" to newPassword)
        )
    }
}
