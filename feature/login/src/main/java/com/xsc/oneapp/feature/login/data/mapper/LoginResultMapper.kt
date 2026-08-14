package com.xsc.oneapp.feature.login.data.mapper

import com.xsc.oneapp.feature.login.data.remote.dto.ForgotPasswordResultDTO
import com.xsc.oneapp.feature.login.data.remote.dto.LoginResultDTO
import com.xsc.oneapp.feature.login.data.remote.dto.ResetPasswordResultDTO
import com.xsc.oneapp.feature.login.data.remote.dto.VerifyOTPResultDTO
import com.xsc.oneapp.feature.login.domain.model.ForgotPasswordResult
import com.xsc.oneapp.feature.login.domain.model.LoginResult
import com.xsc.oneapp.feature.login.domain.model.ResetPasswordResult
import com.xsc.oneapp.feature.login.domain.model.VerifyOTPResult

/**
 * DTO -> domain mapping, consistent with the pattern feature/profile uses.
 * Keeps LoginRepositoryImpl from deserializing straight into domain models.
 */
object LoginResultMapper {

    fun toDomain(dto: LoginResultDTO): LoginResult = LoginResult(
        token = dto.token,
        refreshToken = dto.refreshToken,
        institutionId = dto.user?.institutionId,
        email = dto.user?.email
    )

    fun toDomain(dto: ForgotPasswordResultDTO): ForgotPasswordResult = ForgotPasswordResult(
        resetToken = dto.resetToken,
        message = dto.message ?: ""
    )

    fun toDomain(dto: VerifyOTPResultDTO): VerifyOTPResult = VerifyOTPResult(
        message = dto.message ?: ""
    )

    fun toDomain(dto: ResetPasswordResultDTO): ResetPasswordResult = ResetPasswordResult(
        message = dto.message ?: ""
    )
}
