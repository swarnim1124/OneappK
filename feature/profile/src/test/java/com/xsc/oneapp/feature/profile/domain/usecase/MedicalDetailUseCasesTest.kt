package com.xsc.oneapp.feature.profile.domain.usecase

import com.xsc.oneapp.feature.profile.domain.model.MedicalDetail
import com.xsc.oneapp.feature.profile.domain.repository.ProfileRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicalDetailUseCasesTest {

    private val medicalDetail = MedicalDetail(
        bloodGroupId = 1, bloodGroup = "A+", allergies = emptyList(), chronicConditions = emptyList(),
        medications = emptyList(), height = 170.0, weight = 65.0, disabilityTypeId = null,
        disabilityType = "", doctorName = "Dr. Smith", doctorContact = "9999999999",
        insurancePolicyNo = "POLICY-1"
    )

    @Test
    fun `GetMedicalDetailUseCase delegates to the repository`() = runTest {
        val repository = mockk<ProfileRepository>()
        coEvery { repository.getMedicalDetail(null) } returns medicalDetail

        val result = GetMedicalDetailUseCase(repository).invoke()

        assertEquals(medicalDetail, result)
    }

    @Test
    fun `UpdateMedicalDetailUseCase forwards fields to the repository`() = runTest {
        val repository = mockk<ProfileRepository>()
        val fields = mapOf("weight" to 68.0)
        coEvery { repository.updateMedicalDetail(null, fields) } just Runs

        UpdateMedicalDetailUseCase(repository).invoke(fieldsToUpdate = fields)

        coVerify(exactly = 1) { repository.updateMedicalDetail(null, fields) }
    }
}
