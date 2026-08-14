package com.xsc.oneapp.feature.profile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FamilyDetailDTO(
    @SerializedName("id") val id: Int?,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("middle_name") val middleName: String?,
    @SerializedName("last_name") val lastName: String?,
    @SerializedName("gender_id") val genderId: Int?,
    @SerializedName("gender_name") val genderName: String?,
    @SerializedName("dob") val dob: String?,
    @SerializedName("mobile_no") val mobileNo: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("occupation") val occupation: String?,
    @SerializedName("annual_income") val annualIncome: Double?,
    @SerializedName("photo_doc_id") val photoDocId: Int?,
    @SerializedName("status_id") val statusId: Int?,
    @SerializedName("status_name") val statusName: String?,
    @SerializedName("relationship_type_id") val relationshipTypeId: Int?,
    @SerializedName("relationship_type_name") val relationshipTypeName: String?,
    @SerializedName("is_primary_guardian") val isPrimaryGuardian: Boolean?,
    @SerializedName("is_emergency_contact") val isEmergencyContact: Boolean?
)

data class EmergencyContactDTO(
    @SerializedName("id") val id: Int?,
    @SerializedName("user_id") val userId: Any?,
    @SerializedName("f_name") val firstName: String?,
    @SerializedName("m_name") val middleName: String?,
    @SerializedName("l_name") val lastName: String?,
    @SerializedName("mobile") val mobile: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("is_primary") val isPrimary: Boolean?,
    @SerializedName("status_id") val statusId: Int?
)
