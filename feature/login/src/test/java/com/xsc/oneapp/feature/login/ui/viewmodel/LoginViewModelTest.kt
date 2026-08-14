package com.xsc.oneapp.feature.login.ui.viewmodel

import app.cash.turbine.test
import com.xsc.oneapp.feature.login.domain.model.LoginResult
import com.xsc.oneapp.feature.login.domain.usecase.LoginUseCase
import com.xsc.oneapp.feature.login.ui.effect.LoginEffect
import com.xsc.oneapp.feature.login.ui.event.LoginEvent
import com.xsc.oneapp.feature.login.ui.state.LoginError
import com.xsc.sdk.auth.TokenManager
import com.xsc.sdk.network.APIError
import com.xsc.sdk.network.NetworkFailureReason
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var tokenManager: TokenManager

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        loginUseCase = mockk()
        tokenManager = mockk(relaxed = true)
        // MockK's relaxed mode can't correctly infer a generic StateFlow<Boolean>
        // property - .value comes back as an un-castable Object instead of a real
        // Boolean. Every test that doesn't care about session expiry still needs a
        // real, correctly-typed flow here since LoginViewModel reads it in init{}.
        every { tokenManager.sessionExpired } returns MutableStateFlow(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = LoginViewModel(loginUseCase, tokenManager)

    // --- Client-side validation ---

    @Test
    fun `blank credentials show IncompleteFields and never call the use case`() = runTest {
        val vm = viewModel()

        vm.onEvent(LoginEvent.SubmitLogin)

        assertEquals(LoginError.IncompleteFields, vm.state.value.error)
        coVerify(exactly = 0) { loginUseCase(any()) }
    }

    @Test
    fun `an email-shaped input with no valid domain shows InvalidEmailFormat`() = runTest {
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("student@"))
        vm.onEvent(LoginEvent.PasswordChanged("Student@123"))

        vm.onEvent(LoginEvent.SubmitLogin)

        assertEquals(LoginError.InvalidEmailFormat, vm.state.value.error)
        coVerify(exactly = 0) { loginUseCase(any()) }
    }

    @Test
    fun `a plain username with no at-sign is not treated as a malformed email`() = runTest {
        coEvery { loginUseCase(any()) } returns
            LoginResult(token = "access", refreshToken = "refresh")
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("admin"))
        vm.onEvent(LoginEvent.PasswordChanged("Student@123"))

        vm.effect.test {
            vm.onEvent(LoginEvent.SubmitLogin)
            assertEquals(LoginEffect.NavigateToDashboard, awaitItem())
        }
        assertNull(vm.state.value.error)
    }

    // --- Success path ---

    @Test
    fun `successful login saves tokens and navigates to dashboard`() = runTest {
        coEvery { loginUseCase(any()) } returns
            LoginResult(token = "access", refreshToken = "refresh")
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("student@oneapp.local"))
        vm.onEvent(LoginEvent.PasswordChanged("Student@123"))

        vm.effect.test {
            vm.onEvent(LoginEvent.SubmitLogin)

            assertEquals(LoginEffect.NavigateToDashboard, awaitItem())
        }
        coVerify { tokenManager.saveTokens("access", "refresh") }
        assertFalse(vm.state.value.isLoading)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `a successful login with an institutionId saves it to TokenManager`() = runTest {
        coEvery { loginUseCase(any()) } returns
            LoginResult(token = "access", refreshToken = "refresh", institutionId = 1)
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
            LoginResult(token = "access", refreshToken = "refresh", institutionId = null)
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
    fun `a successful login with an email saves it to TokenManager`() = runTest {
        coEvery { loginUseCase(any()) } returns
            LoginResult(token = "access", refreshToken = "refresh", email = "vivaan.reddy@oneapp.dev")
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("vivaan.reddy@oneapp.dev"))
        vm.onEvent(LoginEvent.PasswordChanged("Student@123"))

        vm.effect.test {
            vm.onEvent(LoginEvent.SubmitLogin)
            awaitItem()
        }

        coVerify { tokenManager.saveEmail("vivaan.reddy@oneapp.dev") }
    }

    @Test
    fun `a successful login without an email never calls saveEmail`() = runTest {
        coEvery { loginUseCase(any()) } returns
            LoginResult(token = "access", refreshToken = "refresh", email = null)
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("student@oneapp.local"))
        vm.onEvent(LoginEvent.PasswordChanged("Student@123"))

        vm.effect.test {
            vm.onEvent(LoginEvent.SubmitLogin)
            awaitItem()
        }

        coVerify(exactly = 0) { tokenManager.saveEmail(any()) }
    }

    @Test
    fun `an empty token in a successful response is treated as an unknown error`() = runTest {
        coEvery { loginUseCase(any()) } returns
            LoginResult(token = "", refreshToken = null)
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("student@oneapp.local"))
        vm.onEvent(LoginEvent.PasswordChanged("Student@123"))

        vm.onEvent(LoginEvent.SubmitLogin)

        assertTrue(vm.state.value.error is LoginError.Unknown)
        coVerify(exactly = 0) { tokenManager.saveTokens(any(), any()) }
    }

    // --- Unclassified/unexpected failures ---

    @Test
    fun `an unexpected exception shows the generic fallback without leaking its message`() = runTest {
        coEvery { loginUseCase(any()) } throws RuntimeException("backend stack trace details")
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("student@oneapp.local"))
        vm.onEvent(LoginEvent.PasswordChanged("wrong-password"))

        vm.onEvent(LoginEvent.SubmitLogin)

        val error = vm.state.value.error
        assertTrue(error is LoginError.Unknown)
        assertEquals("Something went wrong. Try again.", error!!.message)
        assertFalse(vm.state.value.isLoading)
    }

    // --- Business errors, classified by code ---

    @Test
    fun `an account-locked business error code shows AccountLocked`() = runTest {
        coEvery { loginUseCase(any()) } throws APIError.BusinessError("ACCOUNT_LOCKED", "Too many attempts")
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("student@oneapp.local"))
        vm.onEvent(LoginEvent.PasswordChanged("Student@123"))

        vm.onEvent(LoginEvent.SubmitLogin)

        assertEquals(LoginError.AccountLocked, vm.state.value.error)
    }

    @Test
    fun `an invalid-credentials business error code shows InvalidCredentials`() = runTest {
        coEvery { loginUseCase(any()) } throws APIError.BusinessError("INVALID_CREDENTIALS", "Wrong username or password")
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("student@oneapp.local"))
        vm.onEvent(LoginEvent.PasswordChanged("wrong"))

        vm.onEvent(LoginEvent.SubmitLogin)

        assertEquals(LoginError.InvalidCredentials, vm.state.value.error)
    }

    @Test
    fun `an unrecognised business error code falls back to the backend's own message`() = runTest {
        coEvery { loginUseCase(any()) } throws APIError.BusinessError("XSC_UNMAPPED", "This institution has disabled self-service login")
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("student@oneapp.local"))
        vm.onEvent(LoginEvent.PasswordChanged("Student@123"))

        vm.onEvent(LoginEvent.SubmitLogin)

        val error = vm.state.value.error
        assertTrue(error is LoginError.Unknown)
        assertEquals("This institution has disabled self-service login", error!!.message)
    }

    // --- Network failures ---

    @Test
    fun `a timeout NetworkError shows LoginError Network with the timeout message, retryable`() = runTest {
        coEvery { loginUseCase(any()) } throws APIError.NetworkError("timed out", NetworkFailureReason.TIMEOUT)
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("student@oneapp.local"))
        vm.onEvent(LoginEvent.PasswordChanged("Student@123"))

        vm.onEvent(LoginEvent.SubmitLogin)

        val error = vm.state.value.error
        assertTrue(error is LoginError.Network)
        assertEquals("Request timed out. Please retry.", error!!.message)
        assertTrue(error.isRetryable)
    }

    @Test
    fun `a no-connection NetworkError shows the no-internet message`() = runTest {
        coEvery { loginUseCase(any()) } throws APIError.NetworkError("no host", NetworkFailureReason.NO_CONNECTION)
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("student@oneapp.local"))
        vm.onEvent(LoginEvent.PasswordChanged("Student@123"))

        vm.onEvent(LoginEvent.SubmitLogin)

        assertEquals("No internet connection. Check your network.", vm.state.value.error?.message)
    }

    // --- Traced HTTP failures ---

    @Test
    fun `a 403 HttpError shows a traced permission error`() = runTest {
        coEvery { loginUseCase(any()) } throws APIError.HttpError(
            statusCode = 403,
            errorMessage = "Server returned HTTP 403",
            module = "m_AAA",
            submodule = "sm_auth",
            action = "session",
            actionType = "add",
            requiredPermission = "m_AAA.sm_auth.session.add"
        )
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("student@oneapp.local"))
        vm.onEvent(LoginEvent.PasswordChanged("Student@123"))

        vm.onEvent(LoginEvent.SubmitLogin)

        val error = vm.state.value.error
        assertTrue(error is LoginError.Traced)
        error as LoginError.Traced
        assertTrue(error.message.contains("[403] Insufficient permissions"))
        assertTrue(error.message.contains("Required: m_AAA.sm_auth.session.add"))
        assertFalse(error.isRetryable)
    }

    @Test
    fun `a 500 HttpError is retryable and carries a reference id`() = runTest {
        coEvery { loginUseCase(any()) } throws APIError.HttpError(
            statusCode = 500,
            errorMessage = "Server returned HTTP 500",
            module = "m_AAA",
            submodule = "sm_auth",
            action = "session",
            actionType = "add",
            requiredPermission = "m_AAA.sm_auth.session.add",
            referenceId = "ERR-1720612345"
        )
        val vm = viewModel()
        vm.onEvent(LoginEvent.EmailChanged("student@oneapp.local"))
        vm.onEvent(LoginEvent.PasswordChanged("Student@123"))

        vm.onEvent(LoginEvent.SubmitLogin)

        val error = vm.state.value.error as LoginError.Traced
        assertTrue(error.message.contains("Reference: ERR-1720612345"))
        assertTrue(error.isRetryable)
    }

    // --- Error clearing behaviour ---

    @Test
    fun `submitting again clears the previous error before validating`() = runTest {
        val vm = viewModel()
        vm.onEvent(LoginEvent.SubmitLogin)
        assertEquals(LoginError.IncompleteFields, vm.state.value.error)

        coEvery { loginUseCase(any()) } returns
            LoginResult(token = "access", refreshToken = "refresh")
        vm.onEvent(LoginEvent.EmailChanged("student@oneapp.local"))
        vm.onEvent(LoginEvent.PasswordChanged("Student@123"))

        vm.effect.test {
            vm.onEvent(LoginEvent.SubmitLogin)
            awaitItem()
        }
        assertNull(vm.state.value.error)
    }

    @Test
    fun `a keystroke never clears a pending error`() = runTest {
        val vm = viewModel()
        vm.onEvent(LoginEvent.SubmitLogin)
        assertEquals(LoginError.IncompleteFields, vm.state.value.error)

        vm.onEvent(LoginEvent.EmailChanged("s"))

        assertEquals(LoginError.IncompleteFields, vm.state.value.error)
    }

    @Test
    fun `navigating away clears a pending error`() = runTest {
        val vm = viewModel()
        vm.onEvent(LoginEvent.SubmitLogin)
        assertEquals(LoginError.IncompleteFields, vm.state.value.error)

        vm.onEvent(LoginEvent.NavigatingAway)

        assertNull(vm.state.value.error)
    }

    // --- Session expiry ---

    @Test
    fun `a forced session expiry shows SessionExpired on arrival and is consumed`() = runTest {
        every { tokenManager.sessionExpired } returns MutableStateFlow(true)

        val vm = viewModel()

        assertEquals(LoginError.SessionExpired, vm.state.value.error)
        verify { tokenManager.consumeSessionExpired() }
    }

    @Test
    fun `no session expiry means no error on arrival`() = runTest {
        every { tokenManager.sessionExpired } returns MutableStateFlow(false)

        val vm = viewModel()

        assertNull(vm.state.value.error)
    }
}
