package com.xsc.oneapp.feature.login.domain.usecase

import com.xsc.oneapp.feature.login.domain.model.ForgotPasswordResult
import com.xsc.oneapp.feature.login.domain.repository.LoginRepository
import javax.inject.Inject

/** m_AAA_sm_auth.py's passwordReset/add step only accepts "email" - no SMS/phone
 * variant exists on this endpoint (confirmed against the real source, 2026-07-31). */
class ForgotPasswordUseCase @Inject constructor(
    private val repository: LoginRepository
) {
    suspend operator fun invoke(email: String): ForgotPasswordResult {
        return repository.forgotPassword(mapOf("email" to email))
    }
}
