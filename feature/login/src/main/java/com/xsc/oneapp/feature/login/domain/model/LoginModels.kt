package com.xsc.oneapp.feature.login.domain.model

data class LoginResult(
    val token: String?,
    val refreshToken: String? = null,
    val institutionId: Int? = null,
    val email: String? = null
)

data class ForgotPasswordResult(
    val resetToken: String?,
    val message: String
)

data class VerifyOTPResult(
    val message: String
)

data class ResetPasswordResult(
    val message: String
)
