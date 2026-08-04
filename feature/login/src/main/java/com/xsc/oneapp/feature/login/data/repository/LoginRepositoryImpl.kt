package com.xsc.oneapp.feature.login.data.repository

import com.xsc.oneapp.feature.login.data.mapper.LoginResultMapper
import com.xsc.oneapp.feature.login.data.network.LoginEndpoint
import com.xsc.oneapp.feature.login.data.remote.dto.ForgotPasswordResultDTO
import com.xsc.oneapp.feature.login.data.remote.dto.LoginResultDTO
import com.xsc.oneapp.feature.login.data.remote.dto.ResetPasswordResultDTO
import com.xsc.oneapp.feature.login.data.remote.dto.VerifyOTPResultDTO
import com.xsc.oneapp.feature.login.domain.model.ForgotPasswordResult
import com.xsc.oneapp.feature.login.domain.model.LoginResult
import com.xsc.oneapp.feature.login.domain.model.ResetPasswordResult
import com.xsc.oneapp.feature.login.domain.model.VerifyOTPResult
import com.xsc.oneapp.feature.login.domain.repository.LoginRepository
import com.xsc.sdk.network.APIError
import com.xsc.sdk.network.api.ApiClient
import com.xsc.sdk.network.api.DispatchRequest
import com.xsc.sdk.network.api.DispatchResponse
import com.xsc.sdk.network.api.errorCodeAsString
import com.google.gson.Gson
import javax.inject.Inject

/**
 * Throws the same typed [APIError] hierarchy every other feature module's repository
 * uses (see sdk:XscNetworkSDK's APIClient), instead of bare Exception - so callers
 * can distinguish a backend-declared business failure (BusinessError) from a
 * malformed/unexpected response (HttpError) the same way everywhere in the app.
 */
class LoginRepositoryImpl @Inject constructor(
    private val apiClient: ApiClient,
    private val gson: Gson
) : LoginRepository {

    override suspend fun login(payload: Map<String, Any>): LoginResult {
        val request = DispatchRequest(
            mod = LoginEndpoint.MODULE,
            subMod = LoginEndpoint.SUBMODULE,
            action = LoginEndpoint.Actions.SESSION,
            actionType = LoginEndpoint.ActionTypes.ADD,
            payload = payload
        )
        val response = apiClient.dispatch(request)
        val dispatchResponse = bodyOrThrow(response)
        if (!dispatchResponse.isSuccess) {
            throw APIError.BusinessError(dispatchResponse.errorCodeAsString(), dispatchResponse.message ?: "Login failed")
        }

        val dto = gson.fromJson(dispatchResponse.data, LoginResultDTO::class.java)
            ?: throw APIError.BusinessError("", "Invalid response data")
        return LoginResultMapper.toDomain(dto)
    }

    override suspend fun forgotPassword(payload: Map<String, Any>): ForgotPasswordResult {
        val request = DispatchRequest(
            mod = LoginEndpoint.MODULE,
            subMod = LoginEndpoint.SUBMODULE_AUTH,
            action = LoginEndpoint.Actions.PASSWORD_RESET,
            actionType = LoginEndpoint.ActionTypes.ADD,
            payload = payload
        )
        val response = apiClient.dispatch(request)
        val dispatchResponse = bodyOrThrow(response)
        if (!dispatchResponse.isSuccess) {
            throw APIError.BusinessError(dispatchResponse.errorCodeAsString(), dispatchResponse.message ?: "Forgot password failed")
        }

        val dto = gson.fromJson(dispatchResponse.data, ForgotPasswordResultDTO::class.java)
            ?: throw APIError.BusinessError("", "Invalid response data")
        return LoginResultMapper.toDomain(dto)
    }

    override suspend fun verifyOTP(payload: Map<String, Any>): VerifyOTPResult {
        val request = DispatchRequest(
            mod = LoginEndpoint.MODULE,
            subMod = LoginEndpoint.SUBMODULE_AUTH,
            action = LoginEndpoint.Actions.PASSWORD_RESET,
            actionType = LoginEndpoint.ActionTypes.VIEW,
            payload = payload
        )
        val response = apiClient.dispatch(request)
        val dispatchResponse = bodyOrThrow(response)
        if (!dispatchResponse.isSuccess) {
            throw APIError.BusinessError(dispatchResponse.errorCodeAsString(), dispatchResponse.message ?: "Verification failed")
        }

        val dto = gson.fromJson(dispatchResponse.data, VerifyOTPResultDTO::class.java)
            ?: throw APIError.BusinessError("", "Invalid response data")
        return LoginResultMapper.toDomain(dto)
    }

    override suspend fun resetPassword(payload: Map<String, Any>): ResetPasswordResult {
        val request = DispatchRequest(
            mod = LoginEndpoint.MODULE,
            subMod = LoginEndpoint.SUBMODULE_AUTH,
            action = LoginEndpoint.Actions.PASSWORD_RESET,
            actionType = LoginEndpoint.ActionTypes.UPDATE,
            payload = payload
        )
        val response = apiClient.dispatch(request)
        val dispatchResponse = bodyOrThrow(response)
        if (!dispatchResponse.isSuccess) {
            throw APIError.BusinessError(dispatchResponse.errorCodeAsString(), dispatchResponse.message ?: "Reset password failed")
        }

        val dto = gson.fromJson(dispatchResponse.data, ResetPasswordResultDTO::class.java)
            ?: throw APIError.BusinessError("", "Invalid response data")
        return LoginResultMapper.toDomain(dto)
    }

    private fun bodyOrThrow(response: retrofit2.Response<DispatchResponse>): DispatchResponse =
        response.body() ?: throw APIError.HttpError(response.code(), "Invalid response")
}
