package com.xsc.oneapp.feature.login.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Wire-format response models. These are the only classes allowed to know about
 * backend field names - domain models (LoginModels.kt) stay free of @SerializedName
 * so a backend key rename fails to compile here instead of silently nulling out a
 * domain field at runtime.
 */
data class LoginResultDTO(
    @SerializedName("token") val token: String?,
    @SerializedName("refreshToken") val refreshToken: String?,
    @SerializedName("captchaRequired") val captchaRequired: Boolean?,
    @SerializedName("user") val user: LoginUserDTO?
)

/** AAA_API_CONTRACT.md §3.1 success response's nested "user" object. */
data class LoginUserDTO(
    @SerializedName("userId") val userId: Int?,
    @SerializedName("email") val email: String?,
    @SerializedName("roles") val roles: List<String>?,
    @SerializedName("institutionId") val institutionId: Int?
)

data class ForgotPasswordResultDTO(
    @SerializedName("resetToken") val resetToken: String?,
    @SerializedName("message") val message: String?
)

data class VerifyOTPResultDTO(
    @SerializedName("message") val message: String?
)

data class ResetPasswordResultDTO(
    @SerializedName("message") val message: String?
)
