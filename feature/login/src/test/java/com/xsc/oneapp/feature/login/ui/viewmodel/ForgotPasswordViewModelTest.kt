package com.xsc.oneapp.feature.login.ui.viewmodel

import app.cash.turbine.test
import com.xsc.oneapp.feature.login.domain.model.ForgotPasswordResult
import com.xsc.oneapp.feature.login.domain.usecase.ForgotPasswordUseCase
import com.xsc.oneapp.feature.login.ui.effect.ForgotPasswordEffect
import com.xsc.oneapp.feature.login.ui.event.ForgotPasswordEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    private lateinit var forgotPasswordUseCase: ForgotPasswordUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        forgotPasswordUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = ForgotPasswordViewModel(forgotPasswordUseCase)

    @Test
    fun `blank email shows an error and never calls the use case`() = runTest {
        val vm = viewModel()

        vm.onEvent(ForgotPasswordEvent.SendOTP)

        assertEquals("Please enter your email address", vm.state.value.errorMessage)
    }

    @Test
    fun `sending the reset request calls the use case with the email and emits NavigateToVerifyOtp`() = runTest {
        coEvery { forgotPasswordUseCase("student@oneapp.local") } returns
            ForgotPasswordResult(resetToken = "reset-token", message = "OTP sent to your email")
        val vm = viewModel()
        vm.onEvent(ForgotPasswordEvent.EmailChanged("student@oneapp.local"))

        vm.effect.test {
            vm.onEvent(ForgotPasswordEvent.SendOTP)

            val effect = awaitItem() as ForgotPasswordEffect.NavigateToVerifyOtp
            assertEquals("reset-token", effect.resetToken)
        }
        coVerify(exactly = 1) { forgotPasswordUseCase("student@oneapp.local") }
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `a missing resetToken from the backend falls back to an empty string, not a crash`() = runTest {
        coEvery { forgotPasswordUseCase("student@oneapp.local") } returns
            ForgotPasswordResult(resetToken = null, message = "OTP sent")
        val vm = viewModel()
        vm.onEvent(ForgotPasswordEvent.EmailChanged("student@oneapp.local"))

        vm.effect.test {
            vm.onEvent(ForgotPasswordEvent.SendOTP)

            val effect = awaitItem() as ForgotPasswordEffect.NavigateToVerifyOtp
            assertEquals("", effect.resetToken)
        }
    }

    @Test
    fun `use case failure surfaces a generic retry message`() = runTest {
        coEvery { forgotPasswordUseCase(any()) } throws RuntimeException("network down")
        val vm = viewModel()
        vm.onEvent(ForgotPasswordEvent.EmailChanged("student@oneapp.local"))

        vm.onEvent(ForgotPasswordEvent.SendOTP)

        assertEquals("Failed to send OTP. Please try again.", vm.state.value.errorMessage)
        assertFalse(vm.state.value.isLoading)
    }
}
