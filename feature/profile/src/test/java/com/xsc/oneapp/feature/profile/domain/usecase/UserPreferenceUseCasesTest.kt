package com.xsc.oneapp.feature.profile.domain.usecase

import com.xsc.oneapp.feature.profile.domain.model.UserPreference
import com.xsc.oneapp.feature.profile.domain.repository.ProfileRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UserPreferenceUseCasesTest {

    private val preference = UserPreference(
        language = "en", theme = "light", timezone = "UTC", defaultLandingModule = "m_student"
    )

    @Test
    fun `GetUserPreferenceUseCase delegates to the repository`() = runTest {
        val repository = mockk<ProfileRepository>()
        coEvery { repository.getUserPreference(null) } returns preference

        val result = GetUserPreferenceUseCase(repository).invoke()

        assertEquals(preference, result)
    }

    @Test
    fun `UpdateUserPreferenceUseCase forwards fields to the repository`() = runTest {
        val repository = mockk<ProfileRepository>()
        val fields = mapOf("theme" to "dark")
        coEvery { repository.updateUserPreference(null, fields) } just Runs

        UpdateUserPreferenceUseCase(repository).invoke(fieldsToUpdate = fields)

        coVerify(exactly = 1) { repository.updateUserPreference(null, fields) }
    }
}
