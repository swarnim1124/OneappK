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
import com.xsc.sdk.auth.requiredPermissionFor
import com.xsc.sdk.network.APIError
import com.xsc.sdk.network.NetworkFailureReason
import com.xsc.sdk.network.api.ApiClient
import com.xsc.sdk.network.api.DispatchRequest
import com.xsc.sdk.network.api.DispatchResponse
import com.xsc.sdk.network.api.errorCodeAsString
import com.google.gson.Gson
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * Throws the same typed [APIError] hierarchy every other feature module's repository
 * uses (see sdk:XscNetworkSDK's APIClient), instead of bare Exception - so callers
 * can distinguish a backend-declared business failure (BusinessError) from a
 * malformed/unexpected response (HttpError) the same way everywhere in the app.
 *
 * Login goes through the low-level [ApiClient] (raw `Response<DispatchResponse>`,
 * see its own kdoc for why) rather than the enriched [com.xsc.sdk.network.APIClient]
 * every other feature module uses, so the IOException-catching and
 * [APIError.HttpError] tracing that client does automatically has to happen here
 * instead - [dispatch] is that, shared by all four calls below so each one only
 * states its own mod/subMod/action/actionType once.
 */
class LoginRepositoryImpl @Inject constructor(
    private val apiClient: ApiClient,
    private val gson: Gson
) : LoginRepository {

    override suspend fun login(payload: Map<String, Any>): LoginResult {
        val dispatchResponse = dispatch(
            LoginEndpoint.MODULE, LoginEndpoint.SUBMODULE, LoginEndpoint.Actions.SESSION, LoginEndpoint.ActionTypes.ADD, payload
        )
        if (!dispatchResponse.isSuccess) {
            throw APIError.BusinessError(dispatchResponse.errorCodeAsString(), dispatchResponse.message ?: "Login failed")
        }

        val dto = gson.fromJson(dispatchResponse.data, LoginResultDTO::class.java)
            ?: throw APIError.BusinessError("", "Invalid response data")
        return LoginResultMapper.toDomain(dto)
    }

    override suspend fun forgotPassword(payload: Map<String, Any>): ForgotPasswordResult {
        val dispatchResponse = dispatch(
            LoginEndpoint.MODULE, LoginEndpoint.SUBMODULE_AUTH, LoginEndpoint.Actions.PASSWORD_RESET, LoginEndpoint.ActionTypes.ADD, payload
        )
        if (!dispatchResponse.isSuccess) {
            throw APIError.BusinessError(dispatchResponse.errorCodeAsString(), dispatchResponse.message ?: "Forgot password failed")
        }

        val dto = gson.fromJson(dispatchResponse.data, ForgotPasswordResultDTO::class.java)
            ?: throw APIError.BusinessError("", "Invalid response data")
        return LoginResultMapper.toDomain(dto)
    }

    override suspend fun verifyOTP(payload: Map<String, Any>): VerifyOTPResult {
        val dispatchResponse = dispatch(
            LoginEndpoint.MODULE, LoginEndpoint.SUBMODULE_AUTH, LoginEndpoint.Actions.PASSWORD_RESET, LoginEndpoint.ActionTypes.VIEW, payload
        )
        if (!dispatchResponse.isSuccess) {
            throw APIError.BusinessError(dispatchResponse.errorCodeAsString(), dispatchResponse.message ?: "Verification failed")
        }

        val dto = gson.fromJson(dispatchResponse.data, VerifyOTPResultDTO::class.java)
            ?: throw APIError.BusinessError("", "Invalid response data")
        return LoginResultMapper.toDomain(dto)
    }

    override suspend fun resetPassword(payload: Map<String, Any>): ResetPasswordResult {
        val dispatchResponse = dispatch(
            LoginEndpoint.MODULE, LoginEndpoint.SUBMODULE_AUTH, LoginEndpoint.Actions.PASSWORD_RESET, LoginEndpoint.ActionTypes.UPDATE, payload
        )
        if (!dispatchResponse.isSuccess) {
            throw APIError.BusinessError(dispatchResponse.errorCodeAsString(), dispatchResponse.message ?: "Reset password failed")
        }

        val dto = gson.fromJson(dispatchResponse.data, ResetPasswordResultDTO::class.java)
            ?: throw APIError.BusinessError("", "Invalid response data")
        return LoginResultMapper.toDomain(dto)
    }

    private suspend fun dispatch(
        mod: String,
        subMod: String,
        action: String,
        actionType: String,
        payload: Map<String, Any>
    ): DispatchResponse {
        val request = DispatchRequest(mod, subMod, action, actionType, payload)

        val response = try {
            apiClient.dispatch(request)
        } catch (e: SocketTimeoutException) {
            throw APIError.NetworkError(e.message ?: "Request timed out", NetworkFailureReason.TIMEOUT)
        } catch (e: UnknownHostException) {
            throw APIError.NetworkError(e.message ?: "No internet connection", NetworkFailureReason.NO_CONNECTION)
        } catch (e: IOException) {
            throw APIError.NetworkError(e.message ?: "Unable to reach the server", NetworkFailureReason.UNREACHABLE)
        }

        if (!response.isSuccessful) {
            val code = response.code()
            throw APIError.HttpError(
                statusCode = code,
                errorMessage = "Server returned HTTP $code",
                module = mod,
                submodule = subMod,
                action = action,
                actionType = actionType,
                requiredPermission = requiredPermissionFor(mod, subMod, action, actionType),
                referenceId = if (code >= 500) "ERR-${System.currentTimeMillis()}" else null
            )
        }

        return response.body() ?: throw APIError.HttpError(
            statusCode = response.code(),
            errorMessage = "Invalid response",
            module = mod,
            submodule = subMod,
            action = action,
            actionType = actionType,
            requiredPermission = requiredPermissionFor(mod, subMod, action, actionType)
        )
    }
}
