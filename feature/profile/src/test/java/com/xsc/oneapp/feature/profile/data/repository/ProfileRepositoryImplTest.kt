package com.xsc.oneapp.feature.profile.data.repository

import com.xsc.oneapp.feature.profile.data.remote.datasource.ProfileRemoteDataSource
import com.xsc.oneapp.feature.profile.data.remote.dto.PersonalDetailDTO
import com.xsc.sdk.auth.SessionManager
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ProfileRepositoryImplTest {

    private val personalDetailDto = PersonalDetailDTO(
        userId = 3, studentId = 77, email = "student@oneapp.local", alternateEmail = null, mobile = "9999999999",
        firstName = "Student", middleName = null, lastName = "One", dob = "2000-01-01", genderId = 1,
        photoDocId = null, nationalityId = null, primaryLangId = null, bloodGroupId = null,
        createdAt = "2026-01-01", updatedAt = "2026-01-01"
    )

    @Test
    fun `getPersonalDetail resolves the userId from the real session id, not a hardcoded one`() = runTest {
        val remoteDataSource = mockk<ProfileRemoteDataSource>()
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "3"
        coEvery { remoteDataSource.getPersonalDetail(mapOf("userId" to 3L)) } returns personalDetailDto

        val result = ProfileRepositoryImpl(remoteDataSource, sessionManager).getPersonalDetail(userId = null)

        assertEquals("3", result.userId)
        assertEquals("student@oneapp.local", result.email)
        coVerify { remoteDataSource.getPersonalDetail(mapOf("userId" to 3L)) }
    }

    @Test
    fun `an explicit userId overrides the session's id`() = runTest {
        val remoteDataSource = mockk<ProfileRemoteDataSource>()
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "3"
        coEvery { remoteDataSource.getPersonalDetail(mapOf("userId" to 5L)) } returns
            personalDetailDto.copy(userId = 5)

        val result = ProfileRepositoryImpl(remoteDataSource, sessionManager).getPersonalDetail(userId = "5")

        assertEquals("5", result.userId)
    }

    @Test
    fun `updatePersonalDetail sends the resolved userId and the fields to update`() = runTest {
        val remoteDataSource = mockk<ProfileRemoteDataSource>()
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "3"
        val expectedPayload = mapOf("userId" to 3L, "fieldsToUpdate" to mapOf("mobile" to "8888888888"))
        coEvery { remoteDataSource.updatePersonalDetail(expectedPayload) } just Runs

        ProfileRepositoryImpl(remoteDataSource, sessionManager)
            .updatePersonalDetail(userId = null, fieldsToUpdate = mapOf("mobile" to "8888888888"))

        coVerify(exactly = 1) { remoteDataSource.updatePersonalDetail(expectedPayload) }
    }

    @Test
    fun `no session id and no explicit userId throws instead of silently defaulting`() {
        val remoteDataSource = mockk<ProfileRemoteDataSource>()
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns null

        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking {
                ProfileRepositoryImpl(remoteDataSource, sessionManager).getPersonalDetail(userId = null)
            }
        }
    }
}
