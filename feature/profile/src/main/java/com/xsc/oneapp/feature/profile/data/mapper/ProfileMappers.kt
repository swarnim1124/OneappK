package com.xsc.oneapp.feature.profile.data.mapper

import com.xsc.oneapp.feature.profile.data.remote.dto.*
import com.xsc.oneapp.feature.profile.domain.model.*

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
        gender = genderName ?: gender.orEmpty(),
        photoDocId = photoDocId,
        nationalityId = nationalityId,
        nationality = nationalityName ?: nationality.orEmpty(),
        primaryLangId = primaryLangId,
        primaryLanguage = primaryLanguage ?: primaryLang.orEmpty(),
        maritalStatusId = maritalStatusId,
        maritalStatus = maritalStatus.orEmpty(),
        bloodGroupId = bloodGroupId,
        bloodGroup = bloodGroupName ?: bloodGroup.orEmpty(),
        createdAt = createdAt.orEmpty(),
        updatedAt = updatedAt.orEmpty(),
        addresses = addresses?.map { it.toDomain() } ?: emptyList()
    )
}

fun AddressDTO.toDomain(): Address {
    return Address(
        userAddressId = userAddressId ?: 0,
        isPrimary = isPrimary ?: false,
        status = status.orEmpty(),
        addressType = addressType ?: addrType.orEmpty(),
        number = number.orEmpty(),
        line1 = line1.orEmpty(),
        line2 = line2.orEmpty(),
        city = city.orEmpty(),
        district = district.orEmpty(),
        state = state.orEmpty(),
        country = country.orEmpty(),
        postalCode = pincode ?: postalCode.orEmpty()
    )
}

fun AcademicIdentifierDTO.toDomain(): AcademicIdentifier {
    return AcademicIdentifier(
        type = type.orEmpty(),
        id = id ?: 0,
        identifierTypeId = identifierTypeId ?: 0,
        identifierTypeName = identifierTypeName.orEmpty(),
        identifierValue = identifierValue.orEmpty(),
        statusId = statusId ?: 0,
        statusName = statusName.orEmpty()
    )
}

fun AcademicDetailViewResponseDTO.toDomain(): AcademicDetail {
    return AcademicDetail(
        identifiers = identifiers?.map { it.toDomain() } ?: emptyList(),
        enrollmentNumber = enrollmentNumber.orEmpty(),
        branch = branch.orEmpty(),
        batch = batch.orEmpty(),
        semester = semester,
        section = section.orEmpty(),
        cgpa = cgpa,
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
        genderName = genderName.orEmpty(),
        dob = dob,
        mobileNo = mobileNo.orEmpty(),
        email = email.orEmpty(),
        occupation = occupation.orEmpty(),
        annualIncome = annualIncome ?: 0.0,
        photoDocId = photoDocId,
        statusId = statusId ?: 0,
        statusName = statusName.orEmpty(),
        relationshipTypeId = relationshipTypeId ?: 0,
        relationshipTypeName = relationshipTypeName.orEmpty(),
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
        bloodGroup = bloodGroupName ?: bloodGroup.orEmpty(),
        allergies = allergies ?: emptyList(),
        chronicConditions = chronicConditions ?: emptyList(),
        medications = medications ?: emptyList(),
        height = height,
        weight = weight,
        disabilityTypeId = disabilityTypeId,
        disabilityType = disabilityTypeName ?: disabilityType.orEmpty(),
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
