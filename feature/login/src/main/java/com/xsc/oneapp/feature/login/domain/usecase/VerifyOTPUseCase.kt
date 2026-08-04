package com.xsc.oneapp.feature.login.domain.usecase

import com.xsc.oneapp.feature.login.domain.model.VerifyOTPResult
import com.xsc.oneapp.feature.login.domain.repository.LoginRepository
import javax.inject.Inject

/** m_AAA_sm_auth.py's passwordReset/view step verifies the resetToken itself -
 * there's no separate OTP code, no email/phone in this payload (confirmed against
 * the real source, 2026-07-31). What the user submits on the verify screen IS the
 * token: either the one returned directly in dev builds (email delivery is
 * bypassed) or the one they'd copy from the reset email in production. */
class VerifyOTPUseCase @Inject constructor(
    private val repository: LoginRepository
) {
    suspend operator fun invoke(token: String): VerifyOTPResult {
        return repository.verifyOTP(mapOf("token" to token))
    }
}
