package com.xsc.oneapp.feature.login.domain.usecase

import com.xsc.oneapp.feature.login.domain.model.LoginResult
import com.xsc.oneapp.feature.login.domain.repository.LoginRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginUseCaseTest {

    @Test
    fun `invoke delegates the payload to the repository unchanged`() = runTest {
        val repository = mockk<LoginRepository>()
        val payload = mapOf("username" to "student@oneapp.local", "password" to "Student@123")
        val expected = LoginResult(token = "access", refreshToken = "refresh")
        coEvery { repository.login(payload) } returns expected

        val result = LoginUseCase(repository).invoke(payload)

        assertEquals(expected, result)
        coVerify(exactly = 1) { repository.login(payload) }
    }
}
