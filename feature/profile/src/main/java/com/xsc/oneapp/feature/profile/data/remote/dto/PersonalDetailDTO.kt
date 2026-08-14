package com.xsc.oneapp.feature.profile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PersonalDetailDTO(
    @SerializedName("userId") val userId: Any?,
    @SerializedName("studentId") val studentId: Any?,
    @SerializedName("email") val email: String?,
    @SerializedName("alternateEmail") val alternateEmail: String?,
    @SerializedName("mobile") val mobile: String?,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("middleName") val middleName: String?,
    @SerializedName("lastName") val lastName: String?,
    @SerializedName("dob") val dob: String?,
    @SerializedName("genderId") val genderId: Int?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("genderName") val genderName: String?,
    @SerializedName("photoDocId") val photoDocId: Int?,
    @SerializedName("nationalityId") val nationalityId: Int?,
    @SerializedName("nationality") val nationality: String?,
    @SerializedName("nationalityName") val nationalityName: String?,
    @SerializedName("primaryLangId") val primaryLangId: Int?,
    @SerializedName("primaryLang") val primaryLang: String?,
    @SerializedName("primaryLanguage") val primaryLanguage: String?,
    @SerializedName("maritalStatusId") val maritalStatusId: Int?,
    @SerializedName("maritalStatus") val maritalStatus: String?,
    @SerializedName("bloodGroupId") val bloodGroupId: Int?,
    @SerializedName("bloodGroup") val bloodGroup: String?,
    @SerializedName("bloodGroupName") val bloodGroupName: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("addresses") val addresses: List<AddressDTO>? = emptyList()
)

data class AddressDTO(
    @SerializedName("user_address_id") val userAddressId: Int?,
    @SerializedName("is_primary") val isPrimary: Boolean?,
    @SerializedName("status_id") val statusId: Int?,
    @SerializedName("status") val status: String?,
    @SerializedName("address_id") val addressId: Int?,
    @SerializedName("addr_type_id") val addrTypeId: Int?,
    @SerializedName("addr_type") val addrType: String?,
    @SerializedName("address_type") val addressType: String?,
    @SerializedName("number") val number: String?,
    @SerializedName("line_1") val line1: String?,
    @SerializedName("line_2") val line2: String?,
    @SerializedName("city_id") val cityId: Int?,
    @SerializedName("city") val city: String?,
    @SerializedName("district") val district: String?,
    @SerializedName("state") val state: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("postal_code_id") val postalCodeId: Int?,
    @SerializedName("postal_code") val postalCode: String?,
    @SerializedName("pincode") val pincode: String?
)

data class AcademicIdentifierDTO(
    @SerializedName("type") val type: String?,
    @SerializedName("id") val id: Int?,
    @SerializedName("user_id") val userId: Any?,
    @SerializedName("identifier_type_id") val identifierTypeId: Int?,
    @SerializedName("identifier_type_name") val identifierTypeName: String?,
    @SerializedName("identifier_value") val identifierValue: String?,
    @SerializedName("status_id") val statusId: Int?,
    @SerializedName("status_name") val statusName: String?
)

data class AcademicDetailViewResponseDTO(
    @SerializedName("identifiers") val identifiers: List<AcademicIdentifierDTO>?,
    @SerializedName("enrollmentNumber") val enrollmentNumber: String?,
    @SerializedName("branch") val branch: String?,
    @SerializedName("batch") val batch: String?,
    @SerializedName("semester") val semester: Int?,
    @SerializedName("section") val section: String?,
    @SerializedName("cgpa") val cgpa: Double?,
    @SerializedName("employeeCode") val employeeCode: String?,
    @SerializedName("studentId") val studentId: Any?
)

data class AcademicDetailAddResponseDTO(
    @SerializedName("userId") val userId: Any?,
    @SerializedName("identifiers") val identifiers: List<AcademicIdentifierDTO>?
)
