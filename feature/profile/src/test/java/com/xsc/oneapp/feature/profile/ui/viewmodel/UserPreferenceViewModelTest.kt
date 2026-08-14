package com.xsc.oneapp.feature.profile.ui.viewmodel

import com.xsc.oneapp.feature.profile.domain.model.UserPreference
import com.xsc.oneapp.feature.profile.domain.usecase.GetUserPreferenceUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.ResetUserPreferenceUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.UpdateUserPreferenceUseCase
import com.xsc.oneapp.feature.profile.ui.state.UserPreferenceEvent
import com.xsc.oneapp.feature.profile.ui.state.UserPreferenceState
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserPreferenceViewModelTest {

    private lateinit var getUserPreferenceUseCase: GetUserPreferenceUseCase
    private lateinit var updateUserPreferenceUseCase: UpdateUserPreferenceUseCase
    private lateinit var resetUserPreferenceUseCase: ResetUserPreferenceUseCase

    private val preference = UserPreference(
        language = "en", theme = "light", timezone = "UTC", defaultLandingModule = "m_student"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getUserPreferenceUseCase = mockk()
        updateUserPreferenceUseCase = mockk()
        resetUserPreferenceUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = UserPreferenceViewModel(
        getUserPreferenceUseCase, updateUserPreferenceUseCase, resetUserPreferenceUseCase
    )

    @Test
    fun `loading preferences surfaces a Success state`() = runTest {
        coEvery { getUserPreferenceUseCase(null) } returns preference
        val vm = viewModel()

        vm.onEvent(UserPreferenceEvent.LoadUserPreference)

        val state = vm.state.value as UserPreferenceState.Success
        assertEquals(preference, state.userPreference)
    }

    @Test
    fun `saving reloads preferences on success`() = runTest {
        coEvery { getUserPreferenceUseCase(null) } returns preference
        coEvery { updateUserPreferenceUseCase(fieldsToUpdate = mapOf("theme" to "dark")) } just Runs
        val vm = viewModel()

        vm.onEvent(UserPreferenceEvent.SaveUserPreference(mapOf("theme" to "dark")))

        assertTrue(vm.state.value is UserPreferenceState.Success)
    }

    @Test
    fun `reset calls resetUserPreferenceUseCase and reloads`() = runTest {
        coEvery { resetUserPreferenceUseCase(null) } just Runs
        coEvery { getUserPreferenceUseCase(null) } returns preference
        val vm = viewModel()

        vm.onEvent(UserPreferenceEvent.ResetUserPreference)

        coVerify { resetUserPreferenceUseCase(null) }
        assertTrue(vm.state.value is UserPreferenceState.Success)
    }
}
