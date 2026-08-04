package com.xsc.oneapp.feature.login.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.xsc.oneapp.feature.login.domain.model.ResetPasswordResult
import com.xsc.oneapp.feature.login.domain.usecase.ResetPasswordUseCase
import com.xsc.oneapp.feature.login.ui.event.ResetPasswordEvent
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
class ResetPasswordViewModelTest {

    private lateinit var resetPasswordUseCase: ResetPasswordUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        resetPasswordUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = ResetPasswordViewModel(
        SavedStateHandle(mapOf("resetToken" to "abc")),
        resetPasswordUseCase
    )

    @Test
    fun `mismatched passwords show an inline error and never call the use case`() = runTest {
        val vm = viewModel()
        vm.onEvent(ResetPasswordEvent.NewPasswordChanged("Password@123"))
        vm.onEvent(ResetPasswordEvent.ConfirmPasswordChanged("Different@123"))

        vm.onEvent(ResetPasswordEvent.Submit)

        assertEquals("Passwords don't match", vm.state.value.errorMessage)
    }

    @Test
    fun `a password under 8 characters is rejected client-side`() = runTest {
        val vm = viewModel()
        vm.onEvent(ResetPasswordEvent.NewPasswordChanged("short"))
        vm.onEvent(ResetPasswordEvent.ConfirmPasswordChanged("short"))

        vm.onEvent(ResetPasswordEvent.Submit)

        assertEquals("Password must be at least 8 characters", vm.state.value.errorMessage)
    }

    @Test
    fun `a successful reset sends the resetToken and newPassword, surfacing the use case's message`() = runTest {
        coEvery { resetPasswordUseCase("abc", "NewPass@123") } returns
            ResetPasswordResult("Password reset successfully")
        val vm = viewModel()
        vm.onEvent(ResetPasswordEvent.NewPasswordChanged("NewPass@123"))
        vm.onEvent(ResetPasswordEvent.ConfirmPasswordChanged("NewPass@123"))

        vm.onEvent(ResetPasswordEvent.Submit)

        assertEquals("Password reset successfully", vm.state.value.successMessage)
        assertEquals(false, vm.state.value.isLoading)
    }

    @Test
    fun `a failed reset surfaces an inline error`() = runTest {
        coEvery { resetPasswordUseCase(any(), any()) } throws RuntimeException("boom")
        val vm = viewModel()
        vm.onEvent(ResetPasswordEvent.NewPasswordChanged("NewPass@123"))
        vm.onEvent(ResetPasswordEvent.ConfirmPasswordChanged("NewPass@123"))

        vm.onEvent(ResetPasswordEvent.Submit)

        assertEquals("Failed to reset password. Please try again.", vm.state.value.errorMessage)
    }
}
