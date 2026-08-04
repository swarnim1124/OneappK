package com.xsc.oneapp.feature.profile.data.mapper

import com.xsc.oneapp.feature.profile.data.remote.dto.*
import com.xsc.oneapp.feature.profile.domain.model.*

/** Gson decodes an `Any?`-typed field holding a raw JSON number (e.g. `"userId": 1`)
 * as a Double, so a bare `.toString()` turns it into "1.0" - which the backend then
 * fails to match against the integer ID. Same bug/fix as
 * SessionManager.normalizeIdClaim, applied here since userId/studentId are both
 * declared `Any?` for the same contract reason (examples show bare JSON numbers). */
private fun Any?.asIdString(): String? {
    val double = this as? Double ?: return this?.toString()
    return if (double == Math.floor(double) && !double.isInfinite()) {
        double.toLong().toString()
    } else {
        double.toString()
    }
}

fun PersonalDetailDTO.toDomain(): PersonalDetail {
    return PersonalDetail(
        userId = userId.asIdString() ?: "",
        studentId = studentId.asIdString(),
        email = email.orEmpty(),
        alternateEmail = alternateEmail.orEmpty(),
        mobile = mobile.orEmpty(),
        firstName = firstName.orEmpty(),
        middleName = middleName.orEmpty(),
        lastName = lastName.orEmpty(),
        dob = dob.orEmpty(),
        genderId = genderId ?: 0,
        photoDocId = photoDocId,
        nationalityId = nationalityId,
        primaryLangId = primaryLangId,
        bloodGroupId = bloodGroupId,
        createdAt = createdAt.orEmpty(),
        updatedAt = updatedAt.orEmpty()
    )
}

fun AcademicIdentifierDTO.toDomain(): AcademicIdentifier {
    return AcademicIdentifier(
        type = type.orEmpty(),
        id = id ?: 0,
        identifierTypeId = identifierTypeId ?: 0,
        identifierValue = identifierValue.orEmpty(),
        statusId = statusId ?: 0
    )
}

fun AcademicDetailViewResponseDTO.toDomain(): AcademicDetail {
    return AcademicDetail(
        identifiers = identifiers?.map { it.toDomain() } ?: emptyList(),
        enrollmentNumber = enrollmentNumber.orEmpty(),
        employeeCode = employeeCode.orEmpty(),
        studentId = studentId.asIdString()
    )
}

fun FamilyDetailDTO.toDomain(): FamilyDetail {
    return FamilyDetail(
        id = id ?: 0,
        firstName = firstName.orEmpty(),
        middleName = middleName.orEmpty(),
        lastName = lastName.orEmpty(),
        genderId = genderId,
        dob = dob,
        mobileNo = mobileNo.orEmpty(),
        email = email.orEmpty(),
        occupation = occupation.orEmpty(),
        annualIncome = annualIncome ?: 0.0,
        photoDocId = photoDocId,
        statusId = statusId ?: 0,
        relationshipTypeId = relationshipTypeId ?: 0,
        isPrimaryGuardian = isPrimaryGuardian ?: false,
        isEmergencyContact = isEmergencyContact ?: false
    )
}

fun EmergencyContactDTO.toDomain(): EmergencyContact {
    return EmergencyContact(
        id = id ?: 0,
        firstName = firstName.orEmpty(),
        middleName = middleName.orEmpty(),
        lastName = lastName.orEmpty(),
        mobile = mobile.orEmpty(),
        email = email.orEmpty(),
        isPrimary = isPrimary ?: false,
        statusId = statusId ?: 0
    )
}

fun MedicalDetailDTO.toDomain(): MedicalDetail {
    return MedicalDetail(
        bloodGroupId = bloodGroupId,
        allergies = allergies ?: emptyList(),
        chronicConditions = chronicConditions ?: emptyList(),
        medications = medications ?: emptyList(),
        height = height,
        weight = weight,
        disabilityTypeId = disabilityTypeId,
        doctorName = doctorName.orEmpty(),
        doctorContact = doctorContact.orEmpty(),
        insurancePolicyNo = insurancePolicyNo.orEmpty()
    )
}

fun UserPreferenceDTO.toDomain(): UserPreference {
    return UserPreference(
        language = language ?: "en",
        theme = theme ?: "light",
        timezone = timezone ?: "UTC",
        defaultLandingModule = defaultLandingModule ?: "m_student"
    )
}
