package com.xsc.oneapp.feature.login.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.xsc.oneapp.feature.login.domain.model.VerifyOTPResult
import com.xsc.oneapp.feature.login.domain.usecase.VerifyOTPUseCase
import com.xsc.oneapp.feature.login.ui.effect.VerifyOtpEffect
import com.xsc.oneapp.feature.login.ui.event.VerifyOtpEvent
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VerifyOtpViewModelTest {

    private lateinit var verifyOTPUseCase: VerifyOTPUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        verifyOTPUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(resetToken: String = "abc") =
        VerifyOtpViewModel(SavedStateHandle(mapOf("resetToken" to resetToken)), verifyOTPUseCase)

    @Test
    fun `the token field is pre-filled with the resetToken carried from the forgot-password step`() = runTest {
        val vm = viewModel(resetToken = "abc")

        assertEquals("abc", vm.state.value.otpCode)
    }

    @Test
    fun `blank code shows an inline error and never calls the use case`() = runTest {
        val vm = viewModel(resetToken = "")

        vm.onEvent(VerifyOtpEvent.Submit)

        assertEquals("Please enter the verification code", vm.state.value.errorMessage)
    }

    @Test
    fun `a successful verify sends the token and emits NavigateToResetPassword with it`() = runTest {
        coEvery { verifyOTPUseCase("abc") } returns VerifyOTPResult("OTP verified")
        val vm = viewModel(resetToken = "abc")

        vm.effect.test {
            vm.onEvent(VerifyOtpEvent.Submit)

            val effect = awaitItem() as VerifyOtpEffect.NavigateToResetPassword
            assertEquals("abc", effect.resetToken)
        }
        assertEquals(false, vm.state.value.isLoading)
    }

    @Test
    fun `editing the field before submit sends the edited value, not the original nav arg`() = runTest {
        coEvery { verifyOTPUseCase("edited-token") } returns VerifyOTPResult("OTP verified")
        val vm = viewModel(resetToken = "abc")
        vm.onEvent(VerifyOtpEvent.OtpChanged("edited-token"))

        vm.effect.test {
            vm.onEvent(VerifyOtpEvent.Submit)

            val effect = awaitItem() as VerifyOtpEffect.NavigateToResetPassword
            assertEquals("edited-token", effect.resetToken)
        }
    }

    @Test
    fun `a failed verify surfaces an inline error`() = runTest {
        coEvery { verifyOTPUseCase(any()) } throws RuntimeException("expired")
        val vm = viewModel(resetToken = "abc")

        vm.onEvent(VerifyOtpEvent.Submit)

        assertEquals("Invalid or expired code. Please try again.", vm.state.value.errorMessage)
    }
}
