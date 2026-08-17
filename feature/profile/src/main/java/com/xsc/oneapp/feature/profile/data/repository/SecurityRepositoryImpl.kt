package com.xsc.oneapp.feature.profile.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xsc.oneapp.feature.profile.data.mapper.SecurityMapper
import com.xsc.oneapp.feature.profile.data.network.ProfileEndpoint
import com.xsc.oneapp.feature.profile.data.remote.dto.BackupCodesResultDTO
import com.xsc.oneapp.feature.profile.data.remote.dto.MfaEnrollmentDTO
import com.xsc.oneapp.feature.profile.data.remote.dto.MfaMethodDTO
import com.xsc.oneapp.feature.profile.domain.model.BackupCodesResult
import com.xsc.oneapp.feature.profile.domain.model.MfaEnrollment
import com.xsc.oneapp.feature.profile.domain.model.MfaMethod
import com.xsc.oneapp.feature.profile.domain.repository.SecurityRepository
import com.xsc.sdk.network.APIError
import com.xsc.sdk.network.api.ApiClient
import com.xsc.sdk.network.api.DispatchRequest
import com.xsc.sdk.network.api.DispatchResponse
import com.xsc.sdk.network.api.errorCodeAsString
import javax.inject.Inject

class SecurityRepositoryImpl @Inject constructor(
    private val apiClient: ApiClient,
    private val gson: Gson
) : SecurityRepository {

    // MFA is part of the AAA (Auth) module in the backend
    private val MODULE = "m_AAA"

    override suspend fun getMfaMethods(): List<MfaMethod> {
        val request = DispatchRequest(
            mod = MODULE,
            subMod = ProfileEndpoint.SubModules.MFA,
            action = ProfileEndpoint.Actions.MFA,
            actionType = ProfileEndpoint.ActionTypes.VIEW,
            payload = emptyMap()
        )
        val response = apiClient.dispatch(request)
        val dispatchResponse = bodyOrThrow(response)
        if (!dispatchResponse.isSuccess) {
            throw APIError.BusinessError(dispatchResponse.errorCodeAsString(), dispatchResponse.message ?: "Failed to get MFA methods")
        }

        val type = object : TypeToken<List<MfaMethodDTO>>() {}.type
        val dtos = gson.fromJson<List<MfaMethodDTO>>(dispatchResponse.data, type)
            ?: emptyList()
        return dtos.map { SecurityMapper.toDomain(it) }
    }

    override suspend fun initiateMfaEnrollment(): MfaEnrollment {
        val request = DispatchRequest(
            mod = MODULE,
            subMod = ProfileEndpoint.SubModules.MFA,
            action = ProfileEndpoint.Actions.MFA,
            actionType = ProfileEndpoint.ActionTypes.ADD,
            payload = emptyMap()
        )
        val response = apiClient.dispatch(request)
        val dispatchResponse = bodyOrThrow(response)
        if (!dispatchResponse.isSuccess) {
            throw APIError.BusinessError(dispatchResponse.errorCodeAsString(), dispatchResponse.message ?: "Failed to initiate MFA enrollment")
        }

        val dto = gson.fromJson(dispatchResponse.data, MfaEnrollmentDTO::class.java)
            ?: throw APIError.BusinessError("", "Invalid response data")
        return SecurityMapper.toDomain(dto)
    }

    override suspend fun finalizeMfaEnrollment(challengeId: String, otp: String): Boolean {
        val request = DispatchRequest(
            mod = MODULE,
            subMod = ProfileEndpoint.SubModules.MFA,
            action = ProfileEndpoint.Actions.MFA,
            actionType = ProfileEndpoint.ActionTypes.ADD,
            payload = mapOf(
                "enrollmentChallengeId" to challengeId,
                "response" to otp
            )
        )
        val response = apiClient.dispatch(request)
        val dispatchResponse = bodyOrThrow(response)
        return dispatchResponse.isSuccess
    }

    override suspend fun regenerateBackupCodes(methodId: String): BackupCodesResult {
        val request = DispatchRequest(
            mod = MODULE,
            subMod = ProfileEndpoint.SubModules.MFA,
            action = ProfileEndpoint.Actions.MFA,
            actionType = ProfileEndpoint.ActionTypes.UPDATE,
            payload = mapOf(
                "method_id" to methodId,
                "action" to "regenerate_backup_codes"
            )
        )
        val response = apiClient.dispatch(request)
        val dispatchResponse = bodyOrThrow(response)
        if (!dispatchResponse.isSuccess) {
            throw APIError.BusinessError(dispatchResponse.errorCodeAsString(), dispatchResponse.message ?: "Failed to regenerate backup codes")
        }

        val dto = gson.fromJson(dispatchResponse.data, BackupCodesResultDTO::class.java)
            ?: throw APIError.BusinessError("", "Invalid response data")
        return SecurityMapper.toDomain(dto)
    }

    override suspend fun disableMfa(methodId: String): Boolean {
        val request = DispatchRequest(
            mod = MODULE,
            subMod = ProfileEndpoint.SubModules.MFA,
            action = ProfileEndpoint.Actions.MFA,
            actionType = ProfileEndpoint.ActionTypes.DELETE,
            payload = mapOf(
                "method_id" to methodId
            )
        )
        val response = apiClient.dispatch(request)
        val dispatchResponse = bodyOrThrow(response)
        return dispatchResponse.isSuccess
    }

    private fun bodyOrThrow(response: retrofit2.Response<DispatchResponse>): DispatchResponse =
        response.body() ?: throw APIError.HttpError(response.code(), "Invalid response")
}
