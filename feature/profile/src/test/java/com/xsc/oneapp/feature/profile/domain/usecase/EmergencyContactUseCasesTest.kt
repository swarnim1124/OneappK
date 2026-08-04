package com.xsc.oneapp.feature.profile.domain.usecase

import com.xsc.oneapp.feature.profile.domain.model.EmergencyContact
import com.xsc.oneapp.feature.profile.domain.repository.ProfileRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class EmergencyContactUseCasesTest {

    private val contact = EmergencyContact(
        id = 1, firstName = "Jane", middleName = "", lastName = "Doe",
        mobile = "9999999999", email = "jane@oneapp.local", isPrimary = true, statusId = 1
    )

    @Test
    fun `GetEmergencyContactUseCase delegates to the repository`() = runTest {
        val repository = mockk<ProfileRepository>()
        coEvery { repository.getEmergencyContact(null) } returns listOf(contact)

        val result = GetEmergencyContactUseCase(repository).invoke()

        assertEquals(listOf(contact), result)
    }

    @Test
    fun `UpdateEmergencyContactUseCase forwards id and fields to the repository`() = runTest {
        val repository = mockk<ProfileRepository>()
        val fields = mapOf("mobile" to "8888888888")
        coEvery { repository.updateEmergencyContact(1, fields) } just Runs

        UpdateEmergencyContactUseCase(repository).invoke(1, fields)

        coVerify(exactly = 1) { repository.updateEmergencyContact(1, fields) }
    }
}
