package com.xsc.oneapp.feature.login.ui.viewmodel

import app.cash.turbine.test
import com.xsc.oneapp.feature.login.domain.model.LoginResult
import com.xsc.oneapp.feature.login.domain.usecase.LoginUseCase
import com.xsc.oneapp.feature.login.ui.effect.LoginEffect
import com.xsc.oneapp.feature.login.ui.event.LoginEvent
import com.xsc.sdk.auth.TokenManager
import com.xsc.sdk.network.recaptcha.RecaptchaManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var tokenManager: TokenManager
    private lateinit var recaptchaManager: RecaptchaManager

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        loginUseCase = mockk()
        tokenManager = mockk(relaxed = true)
        recaptchaManager = mockk()
        coEvery { recaptchaManager.execute(any()) } returns ""
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = LoginViewModel(loginUseCase, tokenManager, recaptchaManager)

    @Test
    fun `blank credentials show a toast and never call the use case`() = runTest {
        val vm = viewModel()

        vm.effect.test {
            vm.onEvent(LoginEvent.SubmitLogin)

            val effect = awaitItem() as LoginEffect.ShowToast
            assertTrue(effect.isError)
        }
        coVerify(exactly = 0) { loginUseCase(any()) }
    }

    @Test
    fun `successful login saves tokens and navigates to dashboard`() = runTest {
        coEvery { loginUseCase(any()) } returns
            LoginResult(token = "access", refreshToken = "refresh", captchaRequired = false)
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("student@oneapp.local"))
        vm.onEvent(LoginEvent.PasswordChanged("Student@123"))

        vm.effect.test {
            vm.onEvent(LoginEvent.SubmitLogin)

            assertEquals(LoginEffect.NavigateToDashboard, awaitItem())
        }
        coVerify { tokenManager.saveTokens("access", "refresh") }
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `a successful login with an institutionId saves it to TokenManager`() = runTest {
        coEvery { loginUseCase(any()) } returns
            LoginResult(token = "access", refreshToken = "refresh", captchaRequired = false, institutionId = 1)
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("student@oneapp.local"))
        vm.onEvent(LoginEvent.PasswordChanged("Student@123"))

        vm.effect.test {
            vm.onEvent(LoginEvent.SubmitLogin)
            awaitItem()
        }

        coVerify { tokenManager.saveInstitutionId(1) }
    }

    @Test
    fun `a successful login without an institutionId never calls saveInstitutionId`() = runTest {
        coEvery { loginUseCase(any()) } returns
            LoginResult(token = "access", refreshToken = "refresh", captchaRequired = false, institutionId = null)
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("student@oneapp.local"))
        vm.onEvent(LoginEvent.PasswordChanged("Student@123"))

        vm.effect.test {
            vm.onEvent(LoginEvent.SubmitLogin)
            awaitItem()
        }

        coVerify(exactly = 0) { tokenManager.saveInstitutionId(any()) }
    }

    @Test
    fun `login failure shows a user-facing error without leaking the exception message`() = runTest {
        coEvery { loginUseCase(any()) } throws RuntimeException("backend stack trace details")
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("student@oneapp.local"))
        vm.onEvent(LoginEvent.PasswordChanged("wrong-password"))

        vm.effect.test {
            vm.onEvent(LoginEvent.SubmitLogin)

            val effect = awaitItem() as LoginEffect.ShowToast
            assertTrue(effect.isError)
            assertFalse(effect.message.contains("backend stack trace details"))
        }
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `a non-blank recaptcha token is sent under the 'captchaToken' key per AAA_API_CONTRACT`() = runTest {
        coEvery { recaptchaManager.execute(any()) } returns "CAPTCHA-TOKEN-XYZ"
        val payloadSlot = slot<Map<String, Any>>()
        coEvery { loginUseCase(capture(payloadSlot)) } returns
            LoginResult(token = "access", refreshToken = "refresh", captchaRequired = false)
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("student@oneapp.local"))
        vm.onEvent(LoginEvent.PasswordChanged("Student@123"))

        vm.effect.test {
            vm.onEvent(LoginEvent.SubmitLogin)
            assertEquals(LoginEffect.ShowToast("CAPTCHA verified successfully!", isError = false), awaitItem())
            assertEquals(LoginEffect.NavigateToDashboard, awaitItem())
        }

        assertEquals("CAPTCHA-TOKEN-XYZ", payloadSlot.captured["captchaToken"])
        assertFalse(payloadSlot.captured.containsKey("captcha"))
    }

    @Test
    fun `a blank recaptcha token is omitted from the payload entirely`() = runTest {
        val payloadSlot = slot<Map<String, Any>>()
        coEvery { loginUseCase(capture(payloadSlot)) } returns
            LoginResult(token = "access", refreshToken = "refresh", captchaRequired = false)
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("student@oneapp.local"))
        vm.onEvent(LoginEvent.PasswordChanged("Student@123"))

        vm.effect.test {
            vm.onEvent(LoginEvent.SubmitLogin)
            awaitItem()
        }

        assertFalse(payloadSlot.captured.containsKey("captchaToken"))
    }

    @Test
    fun `an empty token in a successful response is treated as an unexpected error`() = runTest {
        coEvery { loginUseCase(any()) } returns
            LoginResult(token = "", refreshToken = null, captchaRequired = false)
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("student@oneapp.local"))
        vm.onEvent(LoginEvent.PasswordChanged("Student@123"))

        vm.effect.test {
            vm.onEvent(LoginEvent.SubmitLogin)

            val effect = awaitItem() as LoginEffect.ShowToast
            assertTrue(effect.isError)
        }
        coVerify(exactly = 0) { tokenManager.saveTokens(any(), any()) }
    }
}
