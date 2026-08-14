package com.xsc.oneapp.feature.profile.ui.viewmodel

import com.xsc.oneapp.feature.profile.domain.model.PersonalDetail
import com.xsc.oneapp.feature.profile.domain.usecase.GetPersonalDetailUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.UpdatePersonalDetailUseCase
import com.xsc.oneapp.feature.profile.ui.state.PersonalDetailEvent
import com.xsc.oneapp.feature.profile.ui.state.PersonalDetailState
import com.xsc.sdk.network.APIError
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
class PersonalDetailViewModelTest {

    private lateinit var getPersonalDetailUseCase: GetPersonalDetailUseCase
    private lateinit var updatePersonalDetailUseCase: UpdatePersonalDetailUseCase

    private val personalDetail = PersonalDetail(
        userId = "3", studentId = "77", email = "student@oneapp.local", alternateEmail = "", mobile = "9999999999",
        firstName = "Student", middleName = "", lastName = "One", dob = "2000-01-01", genderId = 1,
        gender = "Male", photoDocId = null, nationalityId = null, nationality = "", primaryLangId = null,
        primaryLanguage = "", maritalStatusId = null, maritalStatus = "", bloodGroupId = null,
        bloodGroup = "", createdAt = "2026-01-01", updatedAt = "2026-01-01", addresses = emptyList()
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getPersonalDetailUseCase = mockk()
        updatePersonalDetailUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = PersonalDetailViewModel(getPersonalDetailUseCase, updatePersonalDetailUseCase)

    @Test
    fun `loading personal detail surfaces a Success state`() = runTest {
        coEvery { getPersonalDetailUseCase() } returns personalDetail
        val vm = viewModel()

        vm.onEvent(PersonalDetailEvent.LoadPersonalDetail)

        val state = vm.state.value as PersonalDetailState.Success
        assertEquals(personalDetail, state.personalDetail)
    }

    @Test
    fun `a business error from the use case maps to BusinessError state`() = runTest {
        coEvery { getPersonalDetailUseCase() } throws APIError.BusinessError("NOT_FOUND", "Profile not found")
        val vm = viewModel()

        vm.onEvent(PersonalDetailEvent.LoadPersonalDetail)

        val state = vm.state.value as PersonalDetailState.BusinessError
        assertEquals("Profile not found", state.message)
    }

    @Test
    fun `a network error from the use case maps to NetworkError state`() = runTest {
        coEvery { getPersonalDetailUseCase() } throws APIError.NetworkError("timeout")
        val vm = viewModel()

        vm.onEvent(PersonalDetailEvent.LoadPersonalDetail)

        assertTrue(vm.state.value is PersonalDetailState.NetworkError)
    }

    @Test
    fun `saving reloads the detail on success`() = runTest {
        coEvery { getPersonalDetailUseCase() } returns personalDetail
        coEvery { updatePersonalDetailUseCase(fieldsToUpdate = any()) } just Runs
        val vm = viewModel()

        vm.onEvent(PersonalDetailEvent.SavePersonalDetail(mapOf("mobile" to "8888888888")))

        coVerify { updatePersonalDetailUseCase(fieldsToUpdate = mapOf("mobile" to "8888888888")) }
        assertTrue(vm.state.value is PersonalDetailState.Success)
    }
}
