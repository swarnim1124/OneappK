package com.xsc.oneapp.feature.login.domain.usecase

import com.xsc.oneapp.feature.login.domain.model.ResetPasswordResult
import com.xsc.oneapp.feature.login.domain.repository.LoginRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ResetPasswordUseCaseTest {

    @Test
    fun `invoke sends only the token and newPassword - passwordReset update takes no email or phone`() = runTest {
        val repository = mockk<LoginRepository>()
        val expected = ResetPasswordResult("Password reset successfully")
        val payload = mapOf("token" to "abc", "newPassword" to "NewPass@123")
        coEvery { repository.resetPassword(payload) } returns expected

        val result = ResetPasswordUseCase(repository).invoke(token = "abc", newPassword = "NewPass@123")

        assertEquals(expected, result)
        coVerify(exactly = 1) { repository.resetPassword(payload) }
    }
}
