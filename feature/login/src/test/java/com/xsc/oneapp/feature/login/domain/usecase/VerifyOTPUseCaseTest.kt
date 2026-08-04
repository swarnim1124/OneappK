package com.xsc.oneapp.feature.login.domain.usecase

import com.xsc.oneapp.feature.login.domain.model.VerifyOTPResult
import com.xsc.oneapp.feature.login.domain.repository.LoginRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class VerifyOTPUseCaseTest {

    @Test
    fun `invoke sends only the token - passwordReset view takes no email or phone`() = runTest {
        val repository = mockk<LoginRepository>()
        val expected = VerifyOTPResult("OTP verified")
        val payload = mapOf("token" to "reset-token-abc")
        coEvery { repository.verifyOTP(payload) } returns expected

        val result = VerifyOTPUseCase(repository).invoke("reset-token-abc")

        assertEquals(expected, result)
        coVerify(exactly = 1) { repository.verifyOTP(payload) }
    }
}
