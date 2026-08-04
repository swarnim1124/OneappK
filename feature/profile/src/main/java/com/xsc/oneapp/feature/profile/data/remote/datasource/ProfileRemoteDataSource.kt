package com.xsc.oneapp.feature.profile.data.remote.datasource

import com.xsc.oneapp.feature.profile.data.remote.dto.*

interface ProfileRemoteDataSource {
    suspend fun addPersonalDetail(payload: Map<String, Any>): PersonalDetailDTO
    suspend fun getPersonalDetail(payload: Map<String, Any>): PersonalDetailDTO
    suspend fun updatePersonalDetail(payload: Map<String, Any>)
    suspend fun deletePersonalDetail(payload: Map<String, Any>)

    suspend fun addAcademicDetail(payload: Map<String, Any>): AcademicDetailAddResponseDTO
    suspend fun getAcademicDetail(payload: Map<String, Any>): AcademicDetailViewResponseDTO
    suspend fun updateAcademicDetail(payload: Map<String, Any>)
    suspend fun deleteAcademicDetail(payload: Map<String, Any>)

    suspend fun addFamilyDetail(payload: Map<String, Any>)
    suspend fun getFamilyDetail(payload: Map<String, Any>): List<FamilyDetailDTO>
    suspend fun updateFamilyDetail(payload: Map<String, Any>)
    suspend fun deleteFamilyDetail(payload: Map<String, Any>)

    suspend fun addEmergencyContact(payload: Map<String, Any>)
    suspend fun getEmergencyContact(payload: Map<String, Any>): List<EmergencyContactDTO>
    suspend fun updateEmergencyContact(payload: Map<String, Any>)
    suspend fun deleteEmergencyContact(payload: Map<String, Any>)

    suspend fun addMedicalDetail(payload: Map<String, Any>)
    suspend fun getMedicalDetail(payload: Map<String, Any>): MedicalDetailDTO?
    suspend fun updateMedicalDetail(payload: Map<String, Any>)
    suspend fun deleteMedicalDetail(payload: Map<String, Any>)

    suspend fun addUserPreference(payload: Map<String, Any>): UserPreferenceDTO
    suspend fun getUserPreference(payload: Map<String, Any>): UserPreferenceDTO
    suspend fun updateUserPreference(payload: Map<String, Any>)
    suspend fun deleteUserPreference(payload: Map<String, Any>): UserPreferenceDTO
}
