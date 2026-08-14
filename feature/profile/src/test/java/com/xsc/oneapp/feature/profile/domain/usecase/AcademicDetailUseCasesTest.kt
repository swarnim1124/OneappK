package com.xsc.oneapp.feature.profile.domain.usecase

import com.xsc.oneapp.feature.profile.domain.model.AcademicDetail
import com.xsc.oneapp.feature.profile.domain.repository.ProfileRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AcademicDetailUseCasesTest {

    private val academicDetail = AcademicDetail(
        identifiers = emptyList(), enrollmentNumber = "ENR-001", branch = "CSE",
        batch = "2022-2026", semester = 5, section = "A", cgpa = 8.32,
        employeeCode = "", studentId = "77"
    )

    @Test
    fun `GetAcademicDetailUseCase delegates to the repository`() = runTest {
        val repository = mockk<ProfileRepository>()
        coEvery { repository.getAcademicDetail(null, null, null) } returns academicDetail

        val result = GetAcademicDetailUseCase(repository).invoke()

        assertEquals(academicDetail, result)
    }

    @Test
    fun `UpdateAcademicIdentifiersUseCase forwards identifiers to the repository`() = runTest {
        val repository = mockk<ProfileRepository>()
        coEvery { repository.updateAcademicIdentifiers(null, "ID-1", "BIO-1") } just Runs

        UpdateAcademicIdentifiersUseCase(repository).invoke(idCardNumber = "ID-1", biometricId = "BIO-1")

        coVerify(exactly = 1) { repository.updateAcademicIdentifiers(null, "ID-1", "BIO-1") }
    }
}
