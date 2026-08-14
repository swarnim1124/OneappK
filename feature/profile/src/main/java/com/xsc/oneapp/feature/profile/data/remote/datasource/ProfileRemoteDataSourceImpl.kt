package com.xsc.oneapp.feature.profile.data.remote.datasource

import com.xsc.oneapp.feature.profile.data.network.ProfileEndpoint
import com.xsc.oneapp.feature.profile.data.remote.dto.*
import com.xsc.sdk.network.APIClient
import javax.inject.Inject

class ProfileRemoteDataSourceImpl @Inject constructor(
    private val apiClient: APIClient
) : ProfileRemoteDataSource {

    override suspend fun addPersonalDetail(payload: Map<String, Any>): PersonalDetailDTO {
        return apiClient.request(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.PERSONAL_DETAIL,
            action = ProfileEndpoint.Actions.PERSONAL_DETAIL,
            actionType = ProfileEndpoint.ActionTypes.ADD,
            payload = payload
        )
    }

    override suspend fun getPersonalDetail(payload: Map<String, Any>): PersonalDetailDTO {
        return apiClient.request(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.PERSONAL_DETAIL,
            action = ProfileEndpoint.Actions.PERSONAL_DETAIL,
            actionType = ProfileEndpoint.ActionTypes.VIEW,
            payload = payload
        )
    }

    override suspend fun updatePersonalDetail(payload: Map<String, Any>) {
        apiClient.request<Unit>(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.PERSONAL_DETAIL,
            action = ProfileEndpoint.Actions.PERSONAL_DETAIL,
            actionType = ProfileEndpoint.ActionTypes.UPDATE,
            payload = payload
        )
    }

    override suspend fun deletePersonalDetail(payload: Map<String, Any>) {
        apiClient.request<Unit>(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.PERSONAL_DETAIL,
            action = ProfileEndpoint.Actions.PERSONAL_DETAIL,
            actionType = ProfileEndpoint.ActionTypes.DELETE,
            payload = payload
        )
    }

    override suspend fun addAcademicDetail(payload: Map<String, Any>): AcademicDetailAddResponseDTO {
        return apiClient.request(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.ACADEMIC_DETAIL,
            action = ProfileEndpoint.Actions.ACADEMIC_DETAIL,
            actionType = ProfileEndpoint.ActionTypes.ADD,
            payload = payload
        )
    }

    override suspend fun getAcademicDetail(payload: Map<String, Any>): AcademicDetailViewResponseDTO {
        return apiClient.request(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.ACADEMIC_DETAIL,
            action = ProfileEndpoint.Actions.ACADEMIC_DETAIL,
            actionType = ProfileEndpoint.ActionTypes.VIEW,
            payload = payload
        )
    }

    override suspend fun updateAcademicDetail(payload: Map<String, Any>) {
        apiClient.request<Unit>(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.ACADEMIC_DETAIL,
            action = ProfileEndpoint.Actions.ACADEMIC_DETAIL,
            actionType = ProfileEndpoint.ActionTypes.UPDATE,
            payload = payload
        )
    }

    override suspend fun deleteAcademicDetail(payload: Map<String, Any>) {
        apiClient.request<Unit>(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.ACADEMIC_DETAIL,
            action = ProfileEndpoint.Actions.ACADEMIC_DETAIL,
            actionType = ProfileEndpoint.ActionTypes.DELETE,
            payload = payload
        )
    }

    override suspend fun addFamilyDetail(payload: Map<String, Any>) {
        apiClient.request<Unit>(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.FAMILY_DETAIL,
            action = ProfileEndpoint.Actions.FAMILY_DETAIL,
            actionType = ProfileEndpoint.ActionTypes.ADD,
            payload = payload
        )
    }

    override suspend fun getFamilyDetail(payload: Map<String, Any>): List<FamilyDetailDTO> {
        return apiClient.request(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.FAMILY_DETAIL,
            action = ProfileEndpoint.Actions.FAMILY_DETAIL,
            actionType = ProfileEndpoint.ActionTypes.VIEW,
            payload = payload
        )
    }

    override suspend fun updateFamilyDetail(payload: Map<String, Any>) {
        apiClient.request<Unit>(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.FAMILY_DETAIL,
            action = ProfileEndpoint.Actions.FAMILY_DETAIL,
            actionType = ProfileEndpoint.ActionTypes.UPDATE,
            payload = payload
        )
    }

    override suspend fun deleteFamilyDetail(payload: Map<String, Any>) {
        apiClient.request<Unit>(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.FAMILY_DETAIL,
            action = ProfileEndpoint.Actions.FAMILY_DETAIL,
            actionType = ProfileEndpoint.ActionTypes.DELETE,
            payload = payload
        )
    }

    override suspend fun addEmergencyContact(payload: Map<String, Any>) {
        apiClient.request<Unit>(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.FAMILY_DETAIL,
            action = ProfileEndpoint.Actions.EMERGENCY_CONTACT,
            actionType = ProfileEndpoint.ActionTypes.ADD,
            payload = payload
        )
    }

    override suspend fun getEmergencyContact(payload: Map<String, Any>): List<EmergencyContactDTO> {
        return apiClient.request(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.FAMILY_DETAIL,
            action = ProfileEndpoint.Actions.EMERGENCY_CONTACT,
            actionType = ProfileEndpoint.ActionTypes.VIEW,
            payload = payload
        )
    }

    override suspend fun updateEmergencyContact(payload: Map<String, Any>) {
        apiClient.request<Unit>(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.FAMILY_DETAIL,
            action = ProfileEndpoint.Actions.EMERGENCY_CONTACT,
            actionType = ProfileEndpoint.ActionTypes.UPDATE,
            payload = payload
        )
    }

    override suspend fun deleteEmergencyContact(payload: Map<String, Any>) {
        apiClient.request<Unit>(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.FAMILY_DETAIL,
            action = ProfileEndpoint.Actions.EMERGENCY_CONTACT,
            actionType = ProfileEndpoint.ActionTypes.DELETE,
            payload = payload
        )
    }

    override suspend fun addMedicalDetail(payload: Map<String, Any>) {
        apiClient.request<Unit>(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.MEDICAL_DETAIL,
            action = ProfileEndpoint.Actions.MEDICAL_DETAIL,
            actionType = ProfileEndpoint.ActionTypes.ADD,
            payload = payload
        )
    }

    /** APIClient.request<T> already returns null for a nullable T when the backend
     * responds success with omitted/null data (m_profile medicalDetail:view -> "No
     * medical detail found" is its own motivating example) - no extra handling needed. */
    override suspend fun getMedicalDetail(payload: Map<String, Any>): MedicalDetailDTO? =
        apiClient.request(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.MEDICAL_DETAIL,
            action = ProfileEndpoint.Actions.MEDICAL_DETAIL,
            actionType = ProfileEndpoint.ActionTypes.VIEW,
            payload = payload
        )

    override suspend fun updateMedicalDetail(payload: Map<String, Any>) {
        apiClient.request<Unit>(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.MEDICAL_DETAIL,
            action = ProfileEndpoint.Actions.MEDICAL_DETAIL,
            actionType = ProfileEndpoint.ActionTypes.UPDATE,
            payload = payload
        )
    }

    override suspend fun deleteMedicalDetail(payload: Map<String, Any>) {
        apiClient.request<Unit>(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.MEDICAL_DETAIL,
            action = ProfileEndpoint.Actions.MEDICAL_DETAIL,
            actionType = ProfileEndpoint.ActionTypes.DELETE,
            payload = payload
        )
    }

    override suspend fun addUserPreference(payload: Map<String, Any>): UserPreferenceDTO {
        return apiClient.request(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.SETTINGS,
            action = ProfileEndpoint.Actions.USER_PREFERENCE,
            actionType = ProfileEndpoint.ActionTypes.ADD,
            payload = payload
        )
    }

    override suspend fun getUserPreference(payload: Map<String, Any>): UserPreferenceDTO {
        return apiClient.request(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.SETTINGS,
            action = ProfileEndpoint.Actions.USER_PREFERENCE,
            actionType = ProfileEndpoint.ActionTypes.VIEW,
            payload = payload
        )
    }

    override suspend fun updateUserPreference(payload: Map<String, Any>) {
        apiClient.request<Unit>(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.SETTINGS,
            action = ProfileEndpoint.Actions.USER_PREFERENCE,
            actionType = ProfileEndpoint.ActionTypes.UPDATE,
            payload = payload
        )
    }

    override suspend fun deleteUserPreference(payload: Map<String, Any>): UserPreferenceDTO {
        return apiClient.request(
            module = ProfileEndpoint.MODULE,
            submodule = ProfileEndpoint.SubModules.SETTINGS,
            action = ProfileEndpoint.Actions.USER_PREFERENCE,
            actionType = ProfileEndpoint.ActionTypes.DELETE,
            payload = payload
        )
    }
}
