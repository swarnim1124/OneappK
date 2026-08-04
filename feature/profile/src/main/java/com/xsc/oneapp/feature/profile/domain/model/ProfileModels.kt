package com.xsc.oneapp.feature.profile.domain.model

data class PersonalDetail(
    val userId: String,
    /** sch_student.tb_stud.id - NOT the same as [userId] (sch_iam.tb_user.id).
     * Null until the backend resolves it via its user_id join; required for any
     * call that's actually keyed on the student record (e.g. familyDetail). */
    val studentId: String?,
    val email: String,
    val alternateEmail: String,
    val mobile: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val dob: String,
    val genderId: Int,
    val photoDocId: Int?,
    val nationalityId: Int?,
    val primaryLangId: Int?,
    val bloodGroupId: Int?,
    val createdAt: String,
    val updatedAt: String
)

data class AcademicIdentifier(
    val type: String,
    val id: Int,
    val identifierTypeId: Int,
    val identifierValue: String,
    val statusId: Int
)

data class AcademicDetail(
    val identifiers: List<AcademicIdentifier>,
    val enrollmentNumber: String,
    val employeeCode: String,
    /** See PersonalDetail.studentId. */
    val studentId: String?
)

data class FamilyDetail(
    val id: Int,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val genderId: Int?,
    val dob: String?,
    val mobileNo: String,
    val email: String,
    val occupation: String,
    val annualIncome: Double,
    val photoDocId: Int?,
    val statusId: Int,
    val relationshipTypeId: Int,
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
    val allergies: List<String>,
    val chronicConditions: List<String>,
    val medications: List<String>,
    val height: Double?,
    val weight: Double?,
    val disabilityTypeId: Int?,
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
