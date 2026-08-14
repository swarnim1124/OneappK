package com.xsc.oneapp.feature.profile.domain.usecase

import com.xsc.oneapp.feature.profile.domain.model.PersonalDetail
import com.xsc.oneapp.feature.profile.domain.repository.ProfileRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PersonalDetailUseCasesTest {

    private val personalDetail = PersonalDetail(
        userId = "3", studentId = "77", email = "student@oneapp.local", alternateEmail = "", mobile = "9999999999",
        firstName = "Student", middleName = "", lastName = "One", dob = "2000-01-01", genderId = 1,
        gender = "Male", photoDocId = null, nationalityId = null, nationality = "", primaryLangId = null,
        primaryLanguage = "", maritalStatusId = null, maritalStatus = "", bloodGroupId = null,
        bloodGroup = "", createdAt = "2026-01-01", updatedAt = "2026-01-01", addresses = emptyList()
    )

    @Test
    fun `GetPersonalDetailUseCase delegates to the repository`() = runTest {
        val repository = mockk<ProfileRepository>()
        coEvery { repository.getPersonalDetail(null) } returns personalDetail

        val result = GetPersonalDetailUseCase(repository).invoke()

        assertEquals(personalDetail, result)
    }

    @Test
    fun `UpdatePersonalDetailUseCase forwards fields to the repository`() = runTest {
        val repository = mockk<ProfileRepository>()
        val fields = mapOf("mobile" to "8888888888")
        coEvery { repository.updatePersonalDetail(null, fields) } just Runs

        UpdatePersonalDetailUseCase(repository).invoke(fieldsToUpdate = fields)

        coVerify(exactly = 1) { repository.updatePersonalDetail(null, fields) }
    }
}
