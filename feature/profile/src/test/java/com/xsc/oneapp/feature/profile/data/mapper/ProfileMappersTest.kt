package com.xsc.oneapp.feature.profile.data.mapper

import com.xsc.oneapp.feature.profile.data.remote.dto.AcademicDetailViewResponseDTO
import com.xsc.oneapp.feature.profile.data.remote.dto.AddressDTO
import com.xsc.oneapp.feature.profile.data.remote.dto.MedicalDetailDTO
import com.xsc.oneapp.feature.profile.data.remote.dto.PersonalDetailDTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileMappersTest {

    private fun personalDetailDto(userId: Any?, studentId: Any?) = PersonalDetailDTO(
        userId = userId, studentId = studentId, email = null, alternateEmail = null, mobile = null,
        firstName = null, middleName = null, lastName = null, dob = null, genderId = null,
        gender = null, genderName = "Female", photoDocId = null, nationalityId = null,
        nationality = null, nationalityName = "Indian", primaryLangId = null,
        primaryLang = null, primaryLanguage = "English", maritalStatusId = null,
        maritalStatus = null, bloodGroupId = null, bloodGroup = null, bloodGroupName = "O+",
        createdAt = null, updatedAt = null
    )

    @Test
    fun `a Gson-decoded whole-number Double userId maps to a plain integer string`() {
        val result = personalDetailDto(userId = 3.0, studentId = 77.0).toDomain()

        assertEquals("3", result.userId)
        assertEquals("77", result.studentId)
    }

    @Test
    fun `a missing studentId maps to null, not the literal string null`() {
        val result = personalDetailDto(userId = 3.0, studentId = null).toDomain()

        assertNull(result.studentId)
    }

    @Test
    fun `lookup name fields are mapped from genderName, nationalityName, bloodGroupName`() {
        val result = personalDetailDto(userId = 1.0, studentId = null).toDomain()

        assertEquals("Female", result.gender)
        assertEquals("Indian", result.nationality)
        assertEquals("O+", result.bloodGroup)
        assertEquals("English", result.primaryLanguage)
    }

    @Test
    fun `addresses are mapped with pincode fallback to postalCode`() {
        val dto = AddressDTO(
            userAddressId = 1, isPrimary = true, statusId = 1, status = "Active",
            addressId = 10, addrTypeId = 1, addrType = "Permanent", addressType = "Permanent",
            number = "42", line1 = "Main St", line2 = null, cityId = 5, city = "Bangalore",
            district = "Bangalore Urban", state = "Karnataka", country = "India",
            postalCodeId = 12, postalCode = "560001", pincode = "560001"
        )
        val result = dto.toDomain()

        assertEquals("42", result.number)
        assertEquals("Bangalore", result.city)
        assertEquals("560001", result.postalCode)
        assertEquals(true, result.isPrimary)
    }

    @Test
    fun `academicDetail view maps branch, batch, semester, section, cgpa`() {
        val dto = AcademicDetailViewResponseDTO(
            identifiers = emptyList(), enrollmentNumber = "ENR-001",
            branch = "Computer Science", batch = "2022-2026", semester = 5,
            section = "Section A", cgpa = 8.32, employeeCode = null, studentId = 77.0
        )

        val result = dto.toDomain()

        assertEquals("77", result.studentId)
        assertEquals("Computer Science", result.branch)
        assertEquals("2022-2026", result.batch)
        assertEquals(5, result.semester)
        assertEquals("Section A", result.section)
        assertEquals(8.32, result.cgpa!!, 0.001)
    }

    @Test
    fun `medicalDetail maps lookup names from bloodGroupName and disabilityTypeName`() {
        val dto = MedicalDetailDTO(
            userId = 42.0, bloodGroupId = 3, bloodGroup = "O+", bloodGroupName = "O+",
            allergies = listOf("Penicillin"), chronicConditions = null, medications = null,
            height = 175.0, weight = 70.0, disabilityTypeId = null,
            disabilityType = null, disabilityTypeName = null,
            doctorName = "Dr Rao", doctorContact = "9876543211", insurancePolicyNo = "POL-100"
        )

        val result = dto.toDomain()

        assertEquals("O+", result.bloodGroup)
        assertEquals(listOf("Penicillin"), result.allergies)
        assertEquals("Dr Rao", result.doctorName)
    }
}
