package com.xsc.oneapp.feature.profile.data.mapper

import com.xsc.oneapp.feature.profile.data.remote.dto.AcademicDetailViewResponseDTO
import com.xsc.oneapp.feature.profile.data.remote.dto.PersonalDetailDTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * userId/studentId are declared `Any?` on the DTOs because the contract shows bare
 * JSON numbers (`"userId": 1`), not strings - which means Gson decodes them as
 * Double when the app actually parses a real response. These tests construct that
 * Double form directly (bypassing Gson) to prove toDomain() still produces a clean
 * integer string ("77", not "77.0") - the exact bug SessionManager.normalizeIdClaim
 * already had to fix for the same reason on the JWT-claims side.
 */
class ProfileMappersTest {

    private fun personalDetailDto(userId: Any?, studentId: Any?) = PersonalDetailDTO(
        userId = userId, studentId = studentId, email = null, alternateEmail = null, mobile = null,
        firstName = null, middleName = null, lastName = null, dob = null, genderId = null,
        photoDocId = null, nationalityId = null, primaryLangId = null, bloodGroupId = null,
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
    fun `academicDetail view also exposes a Gson-decoded studentId as a plain integer string`() {
        val dto = AcademicDetailViewResponseDTO(
            identifiers = emptyList(), enrollmentNumber = "ENR-001", employeeCode = null, studentId = 77.0
        )

        val result = dto.toDomain()

        assertEquals("77", result.studentId)
    }
}
