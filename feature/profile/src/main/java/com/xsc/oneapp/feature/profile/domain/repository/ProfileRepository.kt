package com.xsc.oneapp.feature.profile.domain.repository

import com.xsc.oneapp.feature.profile.domain.model.*

interface ProfileRepository {
    suspend fun getPersonalDetail(userId: String? = null): PersonalDetail
    suspend fun addPersonalDetail(payload: Map<String, Any>): String
    suspend fun updatePersonalDetail(userId: String? = null, fieldsToUpdate: Map<String, Any>)
    suspend fun deletePersonalDetail(userId: String? = null, reason: String)

    suspend fun getAcademicDetail(userId: String? = null, enrollmentNumber: String? = null, employeeCode: String? = null): AcademicDetail
    suspend fun addAcademicIdentifiers(userId: String? = null, idCardNumber: String? = null, biometricId: String? = null)
    suspend fun updateAcademicIdentifiers(userId: String? = null, idCardNumber: String? = null, biometricId: String? = null)
    suspend fun deleteAcademicIdentifier(userId: String? = null, identifierType: String, reason: String)

    suspend fun getFamilyDetail(studentId: String): List<FamilyDetail>
    suspend fun addFamilyDetail(payload: Map<String, Any>)
    suspend fun updateFamilyDetail(familyDetailId: Int, fieldsToUpdate: Map<String, Any>)
    suspend fun deleteFamilyDetail(familyDetailId: Int, reason: String)

    suspend fun getEmergencyContact(userId: String? = null): List<EmergencyContact>
    suspend fun addEmergencyContact(payload: Map<String, Any>)
    suspend fun updateEmergencyContact(contactId: Int, fieldsToUpdate: Map<String, Any>)
    suspend fun deleteEmergencyContact(contactId: Int)

    suspend fun getMedicalDetail(userId: String? = null): MedicalDetail?
    suspend fun addMedicalDetail(payload: Map<String, Any>)
    suspend fun updateMedicalDetail(userId: String? = null, fieldsToUpdate: Map<String, Any>)
    suspend fun deleteMedicalDetail(userId: String? = null, reason: String)

    suspend fun getUserPreference(userId: String? = null): UserPreference
    suspend fun addUserPreference(userId: String? = null, payload: Map<String, Any>)
    suspend fun updateUserPreference(userId: String? = null, fieldsToUpdate: Map<String, Any>)
    suspend fun resetUserPreference(userId: String? = null)
}
