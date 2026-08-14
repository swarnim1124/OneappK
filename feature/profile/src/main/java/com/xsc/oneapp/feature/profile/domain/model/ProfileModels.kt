package com.xsc.oneapp.feature.profile.domain.model

data class PersonalDetail(
    val userId: String,
    val studentId: String?,
    val email: String,
    val alternateEmail: String,
    val mobile: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val dob: String,
    val genderId: Int,
    val gender: String,
    val photoDocId: Int?,
    val nationalityId: Int?,
    val nationality: String,
    val primaryLangId: Int?,
    val primaryLanguage: String,
    val maritalStatusId: Int?,
    val maritalStatus: String,
    val bloodGroupId: Int?,
    val bloodGroup: String,
    val createdAt: String,
    val updatedAt: String,
    val addresses: List<Address>
)

data class Address(
    val userAddressId: Int,
    val isPrimary: Boolean,
    val status: String,
    val addressType: String,
    val number: String,
    val line1: String,
    val line2: String,
    val city: String,
    val district: String,
    val state: String,
    val country: String,
    val postalCode: String
)

data class AcademicIdentifier(
    val type: String,
    val id: Int,
    val identifierTypeId: Int,
    val identifierTypeName: String,
    val identifierValue: String,
    val statusId: Int,
    val statusName: String
)

data class AcademicDetail(
    val identifiers: List<AcademicIdentifier>,
    val enrollmentNumber: String,
    val branch: String,
    val batch: String,
    val semester: Int?,
    val section: String,
    val cgpa: Double?,
    val employeeCode: String,
    val studentId: String?
)

data class FamilyDetail(
    val id: Int,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val genderId: Int?,
    val genderName: String,
    val dob: String?,
    val mobileNo: String,
    val email: String,
    val occupation: String,
    val annualIncome: Double,
    val photoDocId: Int?,
    val statusId: Int,
    val statusName: String,
    val relationshipTypeId: Int,
    val relationshipTypeName: String,
    val isPrimaryGuardian: Boolean,
    val isEmergencyContact: Boolean
)

data class EmergencyContact(
    val id: Int,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val mobile: String,
    val email: String,
    val isPrimary: Boolean,
    val statusId: Int
)

data class MedicalDetail(
    val bloodGroupId: Int?,
    val bloodGroup: String,
    val allergies: List<String>,
    val chronicConditions: List<String>,
    val medications: List<String>,
    val height: Double?,
    val weight: Double?,
    val disabilityTypeId: Int?,
    val disabilityType: String,
    val doctorName: String,
    val doctorContact: String,
    val insurancePolicyNo: String
)

data class UserPreference(
    val language: String,
    val theme: String,
    val timezone: String,
    val defaultLandingModule: String
)
