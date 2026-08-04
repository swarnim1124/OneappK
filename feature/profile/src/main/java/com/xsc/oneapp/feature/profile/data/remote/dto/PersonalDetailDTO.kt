package com.xsc.oneapp.feature.profile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PersonalDetailDTO(
    @SerializedName("userId") val userId: Any?,
    /** sch_iam.tb_user.id != sch_student.tb_stud.id - the backend now resolves
     * this via a join (WHERE user_id = :logged_in_user_id) and returns it
     * alongside userId (confirmed 2026-07-31) so callers that need the real
     * studentId (e.g. familyDetail) don't have to guess it equals userId. */
    @SerializedName("studentId") val studentId: Any?,
    @SerializedName("email") val email: String?,
    @SerializedName("alternateEmail") val alternateEmail: String?,
    @SerializedName("mobile") val mobile: String?,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("middleName") val middleName: String?,
    @SerializedName("lastName") val lastName: String?,
    @SerializedName("dob") val dob: String?,
    @SerializedName("genderId") val genderId: Int?,
    @SerializedName("photoDocId") val photoDocId: Int?,
    @SerializedName("nationalityId") val nationalityId: Int?,
    @SerializedName("primaryLangId") val primaryLangId: Int?,
    @SerializedName("bloodGroupId") val bloodGroupId: Int?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("addresses") val addresses: List<Any>? = emptyList()
)

data class AcademicIdentifierDTO(
    @SerializedName("type") val type: String?,
    @SerializedName("id") val id: Int?,
    @SerializedName("user_id") val userId: Any?,
    @SerializedName("identifier_type_id") val identifierTypeId: Int?,
    @SerializedName("identifier_value") val identifierValue: String?,
    @SerializedName("status_id") val statusId: Int?
)

data class AcademicDetailViewResponseDTO(
    @SerializedName("identifiers") val identifiers: List<AcademicIdentifierDTO>?,
    @SerializedName("enrollmentNumber") val enrollmentNumber: String?,
    @SerializedName("employeeCode") val employeeCode: String?,
    /** See PersonalDetailDTO.studentId - also exposed here (confirmed 2026-07-31). */
    @SerializedName("studentId") val studentId: Any?
)

data class AcademicDetailAddResponseDTO(
    @SerializedName("userId") val userId: Any?,
    @SerializedName("identifiers") val identifiers: List<AcademicIdentifierDTO>?
)
