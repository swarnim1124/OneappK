package com.xsc.oneapp.feature.login.domain.usecase

import com.xsc.oneapp.feature.login.domain.model.ForgotPasswordResult
import com.xsc.oneapp.feature.login.domain.repository.LoginRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ForgotPasswordUseCaseTest {

    @Test
    fun `invoke sends only the email - passwordReset add takes no phone variant`() = runTest {
        val repository = mockk<LoginRepository>()
        val expected = ForgotPasswordResult(resetToken = "reset-token", message = "OTP sent")
        val payload = mapOf("email" to "student@oneapp.local")
        coEvery { repository.forgotPassword(payload) } returns expected

        val result = ForgotPasswordUseCase(repository).invoke("student@oneapp.local")

        assertEquals(expected, result)
        coVerify(exactly = 1) { repository.forgotPassword(payload) }
    }
}
