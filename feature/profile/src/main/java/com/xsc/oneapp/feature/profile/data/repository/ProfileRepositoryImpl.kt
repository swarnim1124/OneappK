package com.xsc.oneapp.feature.profile.data.repository

import com.xsc.oneapp.feature.profile.data.mapper.toDomain
import com.xsc.oneapp.feature.profile.data.remote.datasource.ProfileRemoteDataSource
import com.xsc.oneapp.feature.profile.domain.model.*
import com.xsc.oneapp.feature.profile.domain.repository.ProfileRepository
import com.xsc.sdk.auth.SessionManager
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val remoteDataSource: ProfileRemoteDataSource,
    private val sessionManager: SessionManager
) : ProfileRepository {

    /**
     * Contract examples show `userId` as a bare JSON number (`"userId": 1`), not a
     * quoted string. SessionManager hands back a String (it's shared with places
     * that need one), so convert it to a Long here whenever it's actually numeric -
     * everything that reads `resolveUserId(...)` only ever puts the result straight
     * into a request payload map, so this doesn't ripple anywhere else.
     */
    private fun resolveUserId(userId: String?): Any {
        val id = userId ?: sessionManager.getUserId() ?: throw IllegalArgumentException("User ID is required")
        return id.toLongOrNull() ?: id
    }

    override suspend fun getPersonalDetail(userId: String?): PersonalDetail {
        val targetUserId = resolveUserId(userId)
        val payload = mapOf("userId" to targetUserId)
        return remoteDataSource.getPersonalDetail(payload).toDomain()
    }

    override suspend fun addPersonalDetail(payload: Map<String, Any>): String {
        // Assume caller provided userId if it's not the current user, or we inject it
        val targetUserId = payload["userId"] ?: resolveUserId(null)
        val finalPayload = payload.toMutableMap()
        finalPayload["userId"] = targetUserId
        
        val result = remoteDataSource.addPersonalDetail(finalPayload)
        return result.userId?.toString() ?: ""
    }

    override suspend fun updatePersonalDetail(userId: String?, fieldsToUpdate: Map<String, Any>) {
        val targetUserId = resolveUserId(userId)
        val payload = mapOf(
            "userId" to targetUserId,
            "fieldsToUpdate" to fieldsToUpdate
        )
        remoteDataSource.updatePersonalDetail(payload)
    }

    override suspend fun deletePersonalDetail(userId: String?, reason: String) {
        val targetUserId = resolveUserId(userId)
        val payload = mapOf(
            "userId" to targetUserId,
            "reason" to reason
        )
        remoteDataSource.deletePersonalDetail(payload)
    }

    override suspend fun getAcademicDetail(userId: String?, enrollmentNumber: String?, employeeCode: String?): AcademicDetail {
        val payload = mutableMapOf<String, Any>()
        if (userId != null || (enrollmentNumber == null && employeeCode == null)) {
            payload["userId"] = resolveUserId(userId)
        } else if (enrollmentNumber != null) {
            payload["enrollmentNumber"] = enrollmentNumber
        } else if (employeeCode != null) {
            payload["employeeCode"] = employeeCode
        }
        return remoteDataSource.getAcademicDetail(payload).toDomain()
    }

    override suspend fun addAcademicIdentifiers(userId: String?, idCardNumber: String?, biometricId: String?) {
        val payload = mutableMapOf<String, Any>("userId" to resolveUserId(userId))
        if (idCardNumber != null) payload["idCardNumber"] = idCardNumber
        if (biometricId != null) payload["biometricId"] = biometricId
        remoteDataSource.addAcademicDetail(payload)
    }

    override suspend fun updateAcademicIdentifiers(userId: String?, idCardNumber: String?, biometricId: String?) {
        val payload = mutableMapOf<String, Any>("userId" to resolveUserId(userId))
        if (idCardNumber != null) payload["idCardNumber"] = idCardNumber
        if (biometricId != null) payload["biometricId"] = biometricId
        remoteDataSource.updateAcademicDetail(payload)
    }

    override suspend fun deleteAcademicIdentifier(userId: String?, identifierType: String, reason: String) {
        val payload = mapOf(
            "userId" to resolveUserId(userId),
            "identifierType" to identifierType,
            "reason" to reason
        )
        remoteDataSource.deleteAcademicDetail(payload)
    }

    override suspend fun getFamilyDetail(studentId: String): List<FamilyDetail> {
        val payload = mapOf("studentId" to studentId)
        return remoteDataSource.getFamilyDetail(payload).map { it.toDomain() }
    }

    override suspend fun addFamilyDetail(payload: Map<String, Any>) {
        remoteDataSource.addFamilyDetail(payload)
    }

    override suspend fun updateFamilyDetail(familyDetailId: Int, fieldsToUpdate: Map<String, Any>) {
        val payload = mapOf(
            "familyDetailId" to familyDetailId,
            "fieldsToUpdate" to fieldsToUpdate
        )
        remoteDataSource.updateFamilyDetail(payload)
    }

    override suspend fun deleteFamilyDetail(familyDetailId: Int, reason: String) {
        val payload = mapOf(
            "familyDetailId" to familyDetailId,
            "reason" to reason
        )
        remoteDataSource.deleteFamilyDetail(payload)
    }

    override suspend fun getEmergencyContact(userId: String?): List<EmergencyContact> {
        val payload = mapOf("userId" to resolveUserId(userId))
        return remoteDataSource.getEmergencyContact(payload).map { it.toDomain() }
    }

    override suspend fun addEmergencyContact(payload: Map<String, Any>) {
        val targetUserId = payload["userId"] ?: resolveUserId(null)
        val finalPayload = payload.toMutableMap()
        finalPayload["userId"] = targetUserId
        remoteDataSource.addEmergencyContact(finalPayload)
    }

    override suspend fun updateEmergencyContact(contactId: Int, fieldsToUpdate: Map<String, Any>) {
        val payload = mapOf(
            "contactId" to contactId,
            "fieldsToUpdate" to fieldsToUpdate
        )
        remoteDataSource.updateEmergencyContact(payload)
    }

    override suspend fun deleteEmergencyContact(contactId: Int) {
        val payload = mapOf("contactId" to contactId)
        remoteDataSource.deleteEmergencyContact(payload)
    }

    override suspend fun getMedicalDetail(userId: String?): MedicalDetail? {
        val payload = mapOf("userId" to resolveUserId(userId))
        return remoteDataSource.getMedicalDetail(payload)?.toDomain()
    }

    override suspend fun addMedicalDetail(payload: Map<String, Any>) {
        val targetUserId = payload["userId"] ?: resolveUserId(null)
        val finalPayload = payload.toMutableMap()
        finalPayload["userId"] = targetUserId
        remoteDataSource.addMedicalDetail(finalPayload)
    }

    override suspend fun updateMedicalDetail(userId: String?, fieldsToUpdate: Map<String, Any>) {
        val payload = mapOf(
            "userId" to resolveUserId(userId),
            "fieldsToUpdate" to fieldsToUpdate
        )
        remoteDataSource.updateMedicalDetail(payload)
    }

    override suspend fun deleteMedicalDetail(userId: String?, reason: String) {
        val payload = mapOf(
            "userId" to resolveUserId(userId),
            "reason" to reason
        )
        remoteDataSource.deleteMedicalDetail(payload)
    }

    override suspend fun getUserPreference(userId: String?): UserPreference {
        val payload = mapOf("userId" to resolveUserId(userId))
        return remoteDataSource.getUserPreference(payload).toDomain()
    }

    override suspend fun addUserPreference(userId: String?, payload: Map<String, Any>) {
        val targetUserId = userId ?: resolveUserId(null)
        val finalPayload = payload.toMutableMap()
        finalPayload["userId"] = targetUserId
        remoteDataSource.addUserPreference(finalPayload)
    }

    override suspend fun updateUserPreference(userId: String?, fieldsToUpdate: Map<String, Any>) {
        val payload = mapOf(
            "userId" to resolveUserId(userId),
            "fieldsToUpdate" to fieldsToUpdate
        )
        remoteDataSource.updateUserPreference(payload)
    }

    override suspend fun resetUserPreference(userId: String?) {
        val payload = mapOf("userId" to resolveUserId(userId))
        remoteDataSource.deleteUserPreference(payload)
    }
}
