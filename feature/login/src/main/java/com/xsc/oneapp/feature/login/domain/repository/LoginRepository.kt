package com.xsc.oneapp.feature.login.domain.repository

import com.xsc.oneapp.feature.login.domain.model.ForgotPasswordResult
import com.xsc.oneapp.feature.login.domain.model.LoginResult
import com.xsc.oneapp.feature.login.domain.model.ResetPasswordResult
import com.xsc.oneapp.feature.login.domain.model.VerifyOTPResult

interface LoginRepository {
    suspend fun login(payload: Map<String, Any>): LoginResult
    suspend fun forgotPassword(payload: Map<String, Any>): ForgotPasswordResult
    suspend fun verifyOTP(payload: Map<String, Any>): VerifyOTPResult
    suspend fun resetPassword(payload: Map<String, Any>): ResetPasswordResult
}
