package com.xsc.oneapp.feature.profile.domain.usecase

import com.xsc.oneapp.feature.profile.domain.model.FamilyDetail
import com.xsc.oneapp.feature.profile.domain.repository.ProfileRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FamilyDetailUseCasesTest {

    private val familyDetail = FamilyDetail(
        id = 1, firstName = "Parent", middleName = "", lastName = "One", genderId = null, dob = null,
        mobileNo = "9999999999", email = "parent@oneapp.local", occupation = "Engineer", annualIncome = 0.0,
        photoDocId = null, statusId = 1, relationshipTypeId = 1, isPrimaryGuardian = true, isEmergencyContact = true
    )

    @Test
    fun `GetFamilyDetailUseCase delegates the student id to the repository`() = runTest {
        val repository = mockk<ProfileRepository>()
        coEvery { repository.getFamilyDetail("3") } returns listOf(familyDetail)

        val result = GetFamilyDetailUseCase(repository).invoke("3")

        assertEquals(listOf(familyDetail), result)
    }

    @Test
    fun `UpdateFamilyDetailUseCase forwards id and fields to the repository`() = runTest {
        val repository = mockk<ProfileRepository>()
        val fields = mapOf("occupation" to "Doctor")
        coEvery { repository.updateFamilyDetail(1, fields) } just Runs

        UpdateFamilyDetailUseCase(repository).invoke(1, fields)

        coVerify(exactly = 1) { repository.updateFamilyDetail(1, fields) }
    }
}
